package main

import spray.json.DefaultJsonProtocol
import main.User
abstract class Message{
}
case class Update(`type`:String, user: String, timestamp: Long,values:Map[String,String]) extends Message
case class AddFriends(`type`:String, user1: String, user2: String)extends Message
case class DeleteFriends(`type`:String, user1: String, user2: String)extends Message

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val test1 = jsonFormat4(Update)
    implicit val test2 = jsonFormat3(AddFriends)
  implicit val test3 = jsonFormat3(DeleteFriends)
implicit val test4 = jsonFormat3(User)


}