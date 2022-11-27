package main

import scala.io.Source

import spray.json._
import DefaultJsonProtocol._

object Main extends App {
  println(args(0))
  if(args.length==0)    throw new Exception("No arguments")

    val users = collection.mutable.Map[String, User]()
  val file = Source.fromFile("tests/"+args(0))
  val lines = file.getLines
    lines.foreach(line=>new WorkerThread(line).start())

  //close the file
  file.close

  import MyJsonProtocol._
  // A thread that is used to parallelize the application.
  final class WorkerThread(line:String) extends Thread {
 override def run(): Unit = handleLine(line)
}

def handleLine(line:String): Unit ={
   var message: Option[Message] = None
    if (line.replaceAll(" ","").startsWith(s"""{"type":"update",""")) message = Some(line.parseJson.convertTo[Update])
    else if (line.replaceAll(" ","").startsWith(s"""{"type":"make_friends",""")) message = Some(line.parseJson.convertTo[AddFriends])
    else if (line.replaceAll(" ","").startsWith(s"""{"type":"del_friends",""")) message = Some(line.parseJson.convertTo[DeleteFriends])
    if(message.isEmpty) return
    message.get match {
      case up: Update => {
        val user = users.get(up.user)
        user match {
          //Critical section
          case None => synchronized{
            users.update(up.user, new User(Array(), up.values, up.values.map(pair=>(pair._1,up.timestamp))))
}
          case us: Some[User] => {
            //Critical section
            this.synchronized{
            var values = us.get.values
            var timestamps = us.get.timestamp
            var updated = false
            up.values.toList.foreach(pair=>
            {
              val timestamp:Long = timestamps.getOrElse(pair._1,0)
              if( timestamp <= up.timestamp){
              updated=true
              values=values.updated(pair._1,pair._2)
              timestamps=timestamps.updated(pair._1,up.timestamp)
              }
          })
            users.update(up.user, new User(us.get.friends, values, timestamps))
            }
            if (us.get.friends.nonEmpty || up.values.nonEmpty) {
              println(s"""{"broadcast":${us.get.friends.toJson} "user": ${up.user}, "timestamp": ${up.timestamp}, "values":${up.values.toJson}}""")
            }
          }
          case _ =>
        }
      }

      case af: AddFriends => {
        users.get(af.user1) match {
          case None => users.update(af.user1, new User(Array(af.user2), Map(), Map()))
          case us: Some[User] => users.update(af.user1, new User(us.get.friends.filterNot(_ == af.user2) :+ af.user2, us.get.values, us.get.timestamp))
          case _ =>
        }
        users.get(af.user2) match {
          case None => users.update(af.user2, new User(Array(af.user1), Map(), Map()))
          case us: Some[User] => users.update(af.user2, new User(us.get.friends.filterNot(_ == af.user1) :+ af.user1, us.get.values, us.get.timestamp))
          case _ =>
        }
      }
      case df: DeleteFriends => {
        users.get(df.user1) match {
          case us: Some[User] => users.update(df.user1, new User(us.get.friends.filterNot(_ == df.user2), us.get.values, us.get.timestamp))
          case _ =>
        }
        users.get(df.user2) match {
          case us: Some[User] => users.update(df.user2, new User(us.get.friends.filterNot(_ == df.user1), us.get.values, us.get.timestamp))
          case _ =>
        }
      }
      case _ =>
    }

}
    println("{")
    println(users.toList.map(pair=> s"""  "${pair._1}": ${pair._2.values.toJson.prettyPrint}""").mkString(",\n"))
      println("}")

}




