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
                        data={"message": mes}
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
                            headers={"Content-Type": "application/json", "Authorization": "key=AIzaSyCGycv7arPP3Re3ts1O7q7paaB5FZJGtxk"}
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
                #if the status for each location is the most common and over 3 votes, add it to message
                if chCrowdedCount>chManageCount and chCrowdedCount>chEmptyCount and chCrowdedCount>=3:
                        message+="Chipotle is currently crowded\n"
                elif chManageCount>chCrowdedCount and chManageCount>chEmptyCount and chManageCount>=3:
                        message+="Chipotle is currently manageable\n"
                elif chEmptyCount>chCrowdedCount and chEmptyCount>chManageCount and chEmptyCount>=3:
                        message+="Chipotle is currently empty\n"
                if stCrowdedCount>stManageCount and stCrowdedCount>stEmptyCount and stCrowdedCount>=3:
                        message+="Starbucks is currently crowded\n"
                elif stManageCount>stCrowdedCount and stManageCount>stEmptyCount and stManageCount>=3:
                        message+="Starbucks is currently manageable\n"
                elif stEmptyCount>stCrowdedCount and stEmptyCount>stManageCount and stEmptyCount>=3:
                        message+="Starbucks is currently empty\n"
                if gymCrowdedCount>gymManageCount and gymCrowdedCount>gymEmptyCount and gymCrowdedCount>=3:
                        message+="Gym is currently crowded\n"
                elif gymManageCount>gymCrowdedCount and gymManageCount>gymEmptyCount and gymManageCount>=3:
                        message+="Gym is currently manageable\n"
                elif gymEmptyCount>gymCrowdedCount and gymEmptyCount>gymManageCount and gymEmptyCount>=3:
                        message+="Gym is currently empty\n"
                if dibCrowdedCount>dibManageCount and dibCrowdedCount>dibEmptyCount and dibCrowdedCount>=3:
                        message+="Dibner is currently crowded\n"
                elif dibManageCount>dibCrowdedCount and dibManageCount>dibEmptyCount and dibManageCount>=3:
                        message+="Dibner is currently manageable\n"
                elif dibEmptyCount>dibCrowdedCount and dibEmptyCount>dibManageCount and dibEmptyCount>=3:
                        message+="Dibner is currently empty\n"
                #if there is a message send it to gcm 
                if message!="":
                        sendMessage(message)



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
                                            headers={"Content-Type": "application/json", "Authorization": "key=AIzaSyCGycv7arPP3Re3ts1O7q7paaB5FZJGtxk"}
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

