import webapp2
import logging
import json
import random
import datetime
from gcm import *
from google.appengine.api import urlfetch
import urllib
                

class MainHandler(webapp2.RequestHandler):
        
	def get(self):
                #sends mesaage to gcm server, takes in string param
                def sendMessage(mes):
                        regIDS=[]
                        data={"message": "FROM SERVER "+mes}
                        headers={"X-Parse-Application-Id": "M1xTL6HzyegVSmshU1NgFbai5VTn07aOMSFmFgvP","X-Parse-REST-API-Key":"cXjwfdojQyN4qQNrh8W7cXOeLgsmJ19dhXs42fz3"}
                        result=urlfetch.fetch(
                            url="https://api.parse.com/1/classes/RegistrationIds",
                            payload='',
                            method=urlfetch.GET,
                            headers=headers)
                        #create list of regids from  parse database query 
                        res=json.loads(result.content)
                        resArr=res["results"]
                        for row in resArr:
                                add=row["regids"]
                                regIDS.append(add)
                        try:
                            form_data={"registration_ids":regIDS,"data":data}
                            headers={"Content-Type": "application/json", "Authorization": "key=AIzaSyCrTeR6MVW3eKBQxknOcpWrnNlI2CSJqng"}
                            result=urlfetch.fetch(
                                    url="https://android.googleapis.com/gcm/send",
                                    payload=json.dumps(form_data),
                                    method=urlfetch.POST,
                                    headers=headers)

                        except urlfetch.Error:
                            logging.exception('Caught exception fetching url')
                            
                #query the database to get all status updates for the last hour
                dt=datetime.datetime.now()-datetime.timedelta(minutes=60)
                dtISO=dt.isoformat()
                params = urllib.urlencode({"where":json.dumps({
                        "createdAt": {
                                "$gte": {
                                        "__type": "Date",
                                        "iso": dtISO
                                        }
                                }
                        })})
                
                #send the get request to parse database
                headers={"X-Parse-Application-Id": "M1xTL6HzyegVSmshU1NgFbai5VTn07aOMSFmFgvP","X-Parse-REST-API-Key":"cXjwfdojQyN4qQNrh8W7cXOeLgsmJ19dhXs42fz3"}
                result=urlfetch.fetch(
                    url="https://api.parse.com/1/classes/Status?%s" % params,
                    payload='',
                    method=urlfetch.GET,
                    headers=headers)
                lastMessage=urlfetch.fetch(
                    url="https://api.parse.com/1/classes/LatestNotification",
                    payload='',
                    method=urlfetch.GET,
                    headers=headers)
                lastMes=json.loads(lastMessage.content)
                mesArr=lastMes["results"]
                self.response.write(json.dumps(result.content))
                res=json.loads(result.content)
                resArr=res["results"]
                #instantiate counts for each status at each location
                chCrowdedCount=0
                stCrowdedCount=0
                gymCrowdedCount=0
                dibCrowdedCount=0
                chEmptyCount=0
                stEmptyCount=0
                gymEmptyCount=0
                dibEmptyCount=0
                chManageCount=0
                stManageCount=0
                gymManageCount=0
                dibManageCount=0
                message=""
                #check the parse db get return value to count each status
                for x in resArr:
                        if x['location']=="Chipotle":
                                if x['status']=="Crowded":
                                        chCrowdedCount+=1
                                elif x['status']=='Manageable':
                                        chManageCount+=1
                                elif x['status']=='Empty':
                                        chEmptyCount+=1
                        elif x['location']=="Starbucks":
                                if x['status']=="Crowded":
                                        stCrowdedCount+=1
                                elif x['status']=='Manageable':
                                        stManageCount+=1
                                elif x['status']=='Empty':
                                        stEmptyCount+=1
                        elif x['location']=="Gym":
                                if x['status']=="Crowded":
                                        gymCrowdedCount+=1
                                elif x['status']=='Manageable':
                                        gymManageCount+=1
                                elif x['status']=='Empty':
                                        gymEmptyCount+=1
                        elif x['location']=="Dibner":
                                if x['status']=="Crowded":
                                        dibCrowdedCount+=1
                                elif x['status']=='Manageable':
                                        dibManageCount+=1
                                elif x['status']=='Empty':
                                        dibEmptyCount+=1
                chMessage=''
                stMessage=''
                gymMessage=''
                dibMessage=''
                #if the status for each location is the most common and over 3 votes, add it to message
                if chCrowdedCount>chManageCount and chCrowdedCount>chEmptyCount and chCrowdedCount>=3:
                        chMessage="Chipotle is currently crowded"
                elif chManageCount>chCrowdedCount and chManageCount>chEmptyCount and chManageCount>=3:
                        chMessage="Chipotle is currently manageable"
                elif chEmptyCount>chCrowdedCount and chEmptyCount>chManageCount and chEmptyCount>=3:
                        chMessage="Chipotle is currently empty"
                if stCrowdedCount>stManageCount and stCrowdedCount>stEmptyCount and stCrowdedCount>=3:
                        stMessage="Starbucks is currently crowded"
                elif stManageCount>stCrowdedCount and stManageCount>stEmptyCount and stManageCount>=3:
                        stMessage="Starbucks is currently manageable"
                elif stEmptyCount>stCrowdedCount and stEmptyCount>stManageCount and stEmptyCount>=3:
                        stMessage="Starbucks is currently empty"
                if gymCrowdedCount>gymManageCount and gymCrowdedCount>gymEmptyCount and gymCrowdedCount>=3:
                        gymMessage="Gym is currently crowded"
                elif gymManageCount>gymCrowdedCount and gymManageCount>gymEmptyCount and gymManageCount>=3:
                        gymMessage="Gym is currently manageable"
                elif gymEmptyCount>gymCrowdedCount and gymEmptyCount>gymManageCount and gymEmptyCount>=3:
                        gymMessage="Gym is currently empty"
                if dibCrowdedCount>dibManageCount and dibCrowdedCount>dibEmptyCount and dibCrowdedCount>=3:
                        dibMessage+="Dibner is currently crowded"
                elif dibManageCount>dibCrowdedCount and dibManageCount>dibEmptyCount and dibManageCount>=3:
                        dibMessage="Dibner is currently manageable"
                elif dibEmptyCount>dibCrowdedCount and dibEmptyCount>dibManageCount and dibEmptyCount>=3:
                        dibMessage="Dibner is currently empty"
                #loop through messages array
                for m in mesArr:
                        #check location and check if the message is the same as the previous notification
                        #if not, send the message to GCM
                        if m['location']=="Chipotle":

                                if m['message']!=chMessage and chMessage!='':
                                        sendMessage(chMessage)
                                        params = urllib.urlencode({"where":json.dumps({
                                                "location": "Chipotle"
                                                })})
                                        headers={"X-Parse-Application-Id": "M1xTL6HzyegVSmshU1NgFbai5VTn07aOMSFmFgvP","X-Parse-REST-API-Key":"cXjwfdojQyN4qQNrh8W7cXOeLgsmJ19dhXs42fz3"}
                                        result=urlfetch.fetch(
                                                url="https://api.parse.com/1/classes/LatestNotification?%s" % params,
                                                payload='',
                                                method=urlfetch.GET,
                                                headers=headers)
                                        res=json.loads(result.content)
                                        resArr=res["results"]
                                        #delete the last notification in parse db
                                        for x in resArr:

                                                objID=x["objectId"]
                                                urlfetch.fetch(
                                                        url="https://api.parse.com/1/classes/LatestNotification/"+objID,
                                                        payload='',
                                                        method=urlfetch.DELETE,
                                                        headers=headers)
                                        form_data={"message":chMessage,"location":"Chipotle"}
                                        #add new notification to parse db
                                        result=urlfetch.fetch(
                                                url="https://api.parse.com/1/classes/LatestNotification",
                                                payload=json.dumps(form_data),
                                                method=urlfetch.POST,
                                                headers=headers)
                        #do the same 
                        if m['location']=="Dibner":
                                if m['message']!=dibMessage and dibMessage!='':
                                        sendMessage(dibMessage)
                                        params = urllib.urlencode({"where":json.dumps({
                                                "location": "Dibner"
                                                })})
                                        headers={"X-Parse-Application-Id": "M1xTL6HzyegVSmshU1NgFbai5VTn07aOMSFmFgvP","X-Parse-REST-API-Key":"cXjwfdojQyN4qQNrh8W7cXOeLgsmJ19dhXs42fz3"}
                                        dibResult=urlfetch.fetch(
                                                url="https://api.parse.com/1/classes/LatestNotification?%s" % params,
                                                payload='',
                                                method=urlfetch.GET,
                                                headers=headers)
                                        dibres=json.loads(dibResult.content)
                                        dibresArr=dibres["results"]


                                        for x in dibresArr:
                                                objID=x["objectId"]
                                                urlfetch.fetch(
                                                        url="https://api.parse.com/1/classes/LatestNotification/"+objID,
                                                        payload='',
                                                        method=urlfetch.DELETE,
                                                        headers=headers)
                                        form_data={"message":dibMessage,"location":"Dibner"}
                                        result=urlfetch.fetch(
                                                url="https://api.parse.com/1/classes/LatestNotification",
                                                payload=json.dumps(form_data),
                                                method=urlfetch.POST,
                                                headers=headers)
                        if m['location']=="Starbucks":
                                if m['message']!=stMessage and stMessage!='':
                                        sendMessage(stMessage)
                                        params = urllib.urlencode({"where":json.dumps({
                                                "location": "Starbucks"
                                                })})
                                        headers={"X-Parse-Application-Id": "M1xTL6HzyegVSmshU1NgFbai5VTn07aOMSFmFgvP","X-Parse-REST-API-Key":"cXjwfdojQyN4qQNrh8W7cXOeLgsmJ19dhXs42fz3"}
                                        result=urlfetch.fetch(
                                                url="https://api.parse.com/1/classes/LatestNotification?%s" % params,
                                                payload='',
                                                method=urlfetch.GET,
                                                headers=headers)
                                        res=json.loads(result.content)
                                        resArr=res["results"]
                                        for x in resArr:
                                                objID=x["objectId"]
                                                urlfetch.fetch(
                                                        url="https://api.parse.com/1/classes/LatestNotification/"+objID,
                                                        payload='',
                                                        method=urlfetch.DELETE,
                                                        headers=headers)
                                        form_data={"message":stMessage,"location":"Starbucks"}
                                        result=urlfetch.fetch(
                                                url="https://api.parse.com/1/classes/LatestNotification",
                                                payload=json.dumps(form_data),
                                                method=urlfetch.POST,
                                                headers=headers)
                        if m['location']=="Gym":
                                if m['message']!=gymMessage and gymMessage!='':
                                        sendMessage(gymMessage)
                                        params = urllib.urlencode({"where":json.dumps({
                                                "location": "Gym"
                                                })})
                                        headers={"X-Parse-Application-Id": "M1xTL6HzyegVSmshU1NgFbai5VTn07aOMSFmFgvP","X-Parse-REST-API-Key":"cXjwfdojQyN4qQNrh8W7cXOeLgsmJ19dhXs42fz3"}
                                        result=urlfetch.fetch(
                                                url="https://api.parse.com/1/classes/LatestNotification?%s" % params,
                                                payload='',
                                                method=urlfetch.GET,
                                                headers=headers)
                                        res=json.loads(result.content)
                                        resArr=res["results"]
        
                                        for x in resArr:
                                                objID=x["objectId"]
                                                urlfetch.fetch(
                                                        url="https://api.parse.com/1/classes/LatestNotification/"+objID,
                                                        payload='',
                                                        method=urlfetch.DELETE,
                                                        headers=headers)
                                        form_data={"message":gymMessage,"location":"Gym"}
                                        result=urlfetch.fetch(
                                                url="https://api.parse.com/1/classes/LatestNotification",
                                                payload=json.dumps(form_data),
                                                method=urlfetch.POST,
                                                headers=headers)                       
                        
                        

	def post(self):
		logging.info("Received POST: " + self.request.body)

		# load json string into python objects
		try:
			inJson = json.loads(self.request.body)
		except:
			inJson = {}
                outJson = {}
                if "command" in inJson:
			command = inJson["command"]
			if command == "SEND_REG_ID":
				# parse python objects with expected fields
				regID = ""
				if "regID" in inJson:
					regID = inJson["regID"]

				if regID != None:
                                        #add regID to the parse database
					outJson["response_code"] = 0
                                        outJson["reg_id"] = regID
                                        self.response.write(json.dumps(outJson))
                                        form_data={"regids":regID}
                                        headers={"X-Parse-Application-Id": "M1xTL6HzyegVSmshU1NgFbai5VTn07aOMSFmFgvP","X-Parse-REST-API-Key":"cXjwfdojQyN4qQNrh8W7cXOeLgsmJ19dhXs42fz3","Content-Type": "application/json"}
                                        result=urlfetch.fetch(
                                            url="https://api.parse.com/1/classes/RegistrationIds",
                                            payload=json.dumps(form_data),
                                            method=urlfetch.POST,
                                            headers=headers)
                                        self.response.write("DB Server response : "+result.content)

				else:
					outJson["response_code"] = -1
					outJson["error_code"] = 2
					outJson["error_string"] = "regID not supplied"
                        
			elif command == "SEND_MESSAGE":
                                regIDS=[]
				message = ""
				data={}
                                headers={"X-Parse-Application-Id": "M1xTL6HzyegVSmshU1NgFbai5VTn07aOMSFmFgvP","X-Parse-REST-API-Key":"cXjwfdojQyN4qQNrh8W7cXOeLgsmJ19dhXs42fz3"}
                                result=urlfetch.fetch(
                                    url="https://api.parse.com/1/classes/RegistrationIds",
                                    payload='',
                                    method=urlfetch.GET,
                                    headers=headers)
                                #create list of regids from  parse database query 
                                res=json.loads(result.content)
                                resArr=res["results"]
                                for row in resArr:
                                        add=row["regids"]
                                        regIDS.append(add)
	
				if "message" in inJson:
                                        message = inJson["message"]
                                        data={"message": message}

				if message != None:
                                        #send post request to gcm server 
                                        try:
                                            form_data={"registration_ids":regIDS,"data":data}
                                            headers={"Content-Type": "application/json", "Authorization": "key=AIzaSyCrTeR6MVW3eKBQxknOcpWrnNlI2CSJqng"}
                                            result=urlfetch.fetch(
                                                    url="https://android.googleapis.com/gcm/send",
                                                    payload=json.dumps(form_data),
                                                    method=urlfetch.POST,
                                                    headers=headers)
                                        except urlfetch.Error:
                                            logging.exception('Caught exception fetching url')	

                                else:
					outJson["response_code"] = -1
					outJson["error_code"] = 3
					outJson["error_string"] = "Message was not supplied"
			else:
				outJson["response_code"] = -1
				outJson["error_code"] = 4
				outJson["error_string"] = "Unrecognized command"
		else:
			outJson["response_code"] = -1
			outJson["error_code"] = 1
			outJson["error_string"] = "Invalid POST request"

		self.response.write(json.dumps(outJson))




app = webapp2.WSGIApplication([
    ('/', MainHandler)
], debug=True)

