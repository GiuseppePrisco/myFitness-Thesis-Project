
# hosted on https://giuseppeprisco.pythonanywhere.com

from flask import Flask, jsonify, request
from flask_sqlalchemy import SQLAlchemy
import json

app = Flask(__name__)

#prepare string to enable database connection
SQLALCHEMY_DATABASE_URI = "mysql+mysqlconnector://{username}:{password}@{hostname}/{databasename}".format(
    username="giuseppeprisco",
    password="my_secret_password",
    hostname="giuseppeprisco.mysql.pythonanywhere-services.com",
    databasename="giuseppeprisco$repetitions",
)

#configure connection parameters
app.config["SQLALCHEMY_DATABASE_URI"] = SQLALCHEMY_DATABASE_URI
app.config["SQLALCHEMY_POOL_RECYCLE"] = 299
app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False

#connect to the database
db = SQLAlchemy(app)


#special class for holding database data
class Rep(db.Model):

    __tablename__ = "repetitions"

    #table columns
    id = db.Column(db.Integer, primary_key=True)
    exerciseName = db.Column(db.String(4096))
    repCount = db.Column(db.Integer)


#starting repetitions used to populate the database
startingReps = {'id':'1','exerciseName':'Pushups','repCount':1000}, {'id':'2','exerciseName':'Squats','repCount':1000}, {'id':'3','exerciseName':'Jumping Jacks','repCount':1000}, {'id':'4','exerciseName':'Pullups','repCount':1000}, {'id':'5','exerciseName':'Situps','repCount':1000}


serverRestarted = False
#serverRestarted = True

if serverRestarted:
    db.drop_all()
    db.create_all()

    for rep in startingReps:
        print("REP", rep)
        rep = Rep(exerciseName=rep["exerciseName"],repCount=rep["repCount"])
        db.session.add(rep)
        db.session.commit()

        #rep = Rep(id=rep["id"],exerciseName=rep["exerciseName"],repCount=rep["repCount"])


#repData = [{"id":str(rep.id),"exerciseName":str(rep.exerciseName), "repCount":rep.repCount} for rep in Rep.query.all()]

#print(repData)
#print(json.dumps(repData))


@app.route('/', methods = ['GET'])
def getRoutes():

    routes = []
    #routes = {}

    for r in app.url_map._rules:

        routes.append([[r.rule],["functionName",r.endpoint],["methods",list(r.methods)]])

        #routes[r.rule] = {}
        #routes[r.rule]["functionName"] = r.endpoint
        #routes[r.rule]["methods"] = list(r.methods)


    #routes.pop("/static/<path:filename>")

    return jsonify(routes)


@app.route('/reps', methods=["GET"])
def getReps():

    #obtain repetition data from database
    repData = [{"id":str(rep.id),"exerciseName":str(rep.exerciseName), "repCount":rep.repCount} for rep in Rep.query.all()]

    #return jsonify(startingReps)
    return jsonify(repData)


@app.route("/reps", methods=["POST"])
def updateReps():

    #data = request.args
    #data1 = request.json

    data = request.get_json()

    #update repetition data
    repetition = Rep.query.get(int(data["id"]))
    repetition.repCount = repetition.repCount + data["repCount"]
    db.session.commit()

    #with startingReps.get_lock():
        #for rep in startingReps:

            #key = rep["id"]

            #if (key == data["id"]):
                #rep["repCount"]+=data["repCount"]


    return jsonify(str(data))


if __name__ == "__main__":
    print("Starting server . . .")
    #app.run()

