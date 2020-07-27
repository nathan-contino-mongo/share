package com.mongodb.realmsnippets

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.bson.types.ObjectId

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import io.realm.OrderedCollectionChangeSet
import io.realm.OrderedRealmCollectionChangeListener

import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration

import io.realm.mongodb.Credentials
import io.realm.mongodb.User
import io.realm.mongodb.sync.SyncConfiguration


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Realm.init(this) // context, usually an Activity or Application
        val appID = "authsample-pdvvn" //"<your app ID>" // replace this with your App ID
        val app: App = App(AppConfiguration.Builder(appID)
            .build())

        val credentials: Credentials = Credentials.anonymous()

        var user: User? = null

        app.loginAsync(credentials) {
            if (it.isSuccess) {
                Log.v("QUICKSTART", "Successfully authenticated anonymously.")
                user = app.currentUser()

                val partitionValue: String = "myPartition"

                val config = SyncConfiguration.Builder(user!!, partitionValue)
                    //.waitForInitialRemoteData()
                    .build()

                val realm: Realm = Realm.getInstance(config)
                // Sync all realm changes via a new instance, and when that instance has been successfully created connect it to an on-screen list (a recycler view)

                val task : Task = Task("New Task", partitionValue)
                realm.executeTransaction { transactionRealm ->
                    transactionRealm.insert(task)
                }

                // all tasks in the realm
                val tasks : RealmResults<Task> = realm.where<Task>().findAll()

                Log.e("EXADS", "size: " + tasks.size)
                Log.e("TEST", "doc" + tasks[0]?.name)

                // you can also filter a collection
                val tasksThatBeginWithN : List<Task> = tasks.where().beginsWith("name", "N").findAll()
                val openTasks : List<Task> = tasks.where().equalTo("status", "Open").findAll()

                val otherTask: Task = tasks[0]!!

                tasks.addChangeListener(OrderedRealmCollectionChangeListener<RealmResults<Task>> { collection, changeSet ->
                    // `null`  means the async query returns the first time.
                    if (changeSet == null) {
                        Log.e("QUICKSTART", "????")
                        return@OrderedRealmCollectionChangeListener
                    }
                    // If you need to react to the deleted indices in some way, handle
                    // in reverse order so you don't have to adjust later indexes to
                    // account for removed data "earlier" in the collection
                    val deletions = changeSet.deletionRanges
                    for (i in deletions.indices.reversed()) {
                        val range = deletions[i]
                        Log.v("QUICKSTART", "Deleted range: ${range.startIndex} to ${range.startIndex + range.length}")
                    }

                    val insertions = changeSet.insertionRanges
                    for (range in insertions) {
                        Log.v("QUICKSTART", "Inserted range: ${range.startIndex} to ${range.startIndex + range.length}")
                    }

                    val modifications = changeSet.changeRanges
                    for (range in modifications) {

                        Log.v("QUICKSTART", "Updated range: ${range.startIndex} to ${range.startIndex + range.length}")
                    }
                })

                // all modifications to a realm must happen inside of a write block
                realm.executeTransaction { transactionRealm ->
                    var innerOtherTask : Task = transactionRealm.where<Task>().equalTo("_id", otherTask._id).findFirst()!!
                    innerOtherTask.status = "Complete"
                }


                val yetAnotherTask: Task = tasks.get(0)!!
                val yetAnotherID = yetAnotherTask._id
                // all modifications to a realm must happen inside of a write block
                realm.executeTransactionAsync { transactionRealm ->
                    var innerYetAnotherTask : Task = transactionRealm.where<Task>().equalTo("_id", yetAnotherID).findFirst()!!
                    innerYetAnotherTask.deleteFromRealm()
                }


                user?.logOutAsync {
                    if (it.isSuccess) {
                        Log.v("QUICKSTART", "Successfully logged out.")
                    } else {
                        Log.e("QUICKSTART", it.error.toString())
                    }
                }
            } else {
                Log.e("QUICKSTART", "an error occurred:")
                Log.e("QUICKSTART", it.error.toString())
            }
        }



    }
}


open class Task(_name: String = "Task", project: String = "My Project") : RealmObject() {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var _partition: String = project
    var name: String = _name

    @Required
    public var status: String = "Open"
}
