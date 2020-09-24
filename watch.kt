import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.bson.types.ObjectId

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import io.realm.OrderedRealmCollectionChangeListener

import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where
import io.realm.log.RealmLog
import io.realm.mongodb.*

import io.realm.mongodb.mongo.MongoClient
import io.realm.mongodb.mongo.MongoCollection
import io.realm.mongodb.mongo.MongoDatabase
import io.realm.mongodb.mongo.events.BaseChangeEvent
import io.realm.mongodb.mongo.iterable.FindIterable
import io.realm.mongodb.mongo.options.UpdateOptions
import io.realm.mongodb.sync.ClientResetRequiredError
import io.realm.mongodb.sync.SyncConfiguration
import io.realm.mongodb.sync.SyncSession
import org.bson.*
import java.util.*


class MainActivity : AppCompatActivity() {

    fun linkUserWithEmailPasswordUser(user: User, email: String, password: String) {
        user.linkCredentialsAsync(Credentials.emailPassword("email", "password")) {
            if (it.isSuccess) {
                Log.v("EXAMPLE", "Successfully linked existing user identity with email/password user: ${it.get().id}")
            } else {
                Log.e("EXAMPLE", "Failed to link user identities with: ${it.error}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Realm.init(this) // context, usually an Activity or Application
        val appID = "unfsck-dmrvz" // replace this with your App ID

        //val handler = SyncSession.ClientResetHandler { session, error -> RealmLog.error("Client Reset required for: " + session.configuration.serverUrl) }
        val app: App = App(AppConfiguration.Builder(appID)
            //.defaultClientResetHandler(handler)
            .build())

        //val otherApp: App = App(AppConfiguration.Builder("unfsck-dmrvz")
        //    .build())

        val credentials: Credentials = Credentials.emailPassword("email", "password")//Credentials.anonymous()

        app.loginAsync(credentials) {
            if (it.isSuccess) {
                Log.v("QUICKSTART", "Successfully authenticated anonymously.")
                val user: User? = app.currentUser()

                /*
                app.emailPassword.registerUserAsync("email", "password") {
                    if (it.isSuccess) {
                        linkUserWithEmailPasswordUser(user!!,"email", "password");
                    } else {
                        Log.e("bad bad thing happen", "bad bad")
                    }
                }*/


                val partitionValue: String = "My Project"

                val config = SyncConfiguration.Builder(user!!, partitionValue)
                    .build()

                val realm: Realm = Realm.getInstance(config)

                val task : Task = Task("New Task", partitionValue)
                realm.executeTransaction { transactionRealm ->
                    transactionRealm.insert(task)
                }

                // all tasks in the realm
                val tasksQuery = realm.where<Task>()
                val tasks : RealmResults<Task> = tasksQuery.findAll()

                // you can also filter a collection
                val tasksThatBeginWithN : List<Task> = tasks.where().beginsWith("name", "N").findAll()
                val openTasks : List<Task> = tasks.where().equalTo("status", TaskStatus.Open.name).findAll()

                val otherTask: Task = tasks[0]!!

                tasks.addChangeListener(OrderedRealmCollectionChangeListener<RealmResults<Task>> { collection, changeSet ->
                    // process deletions in reverse order if maintaining parallel data structures so indices don't change as you iterate
                    val deletions = changeSet.deletionRanges
                    for (i in deletions.indices.reversed()) {
                        val range = deletions[i]
                        Log.v("QUICKSTART", "Deleted range: ${range.startIndex} to ${range.startIndex + range.length - 1}")
                    }

                    val insertions = changeSet.insertionRanges
                    for (range in insertions) {
                        Log.v("QUICKSTART", "Inserted range: ${range.startIndex} to ${range.startIndex + range.length - 1}")
                    }

                    val modifications = changeSet.changeRanges
                    for (range in modifications) {
                        Log.v("QUICKSTART", "Updated range: ${range.startIndex} to ${range.startIndex + range.length - 1}")
                    }
                })

                // all modifications to a realm must happen inside of a write block
                realm.executeTransaction { transactionRealm ->
                    val innerOtherTask : Task = transactionRealm.where<Task>().equalTo("_id", otherTask._id).findFirst()!!
                    innerOtherTask.status = TaskStatus.Complete.name
                }

                val yetAnotherTask: Task = tasks.get(0)!!
                val yetAnotherTaskId: ObjectId = yetAnotherTask._id
                // all modifications to a realm must happen inside of a write block
                realm.executeTransactionAsync { transactionRealm ->
                    val innerYetAnotherTask : Task = transactionRealm.where<Task>().equalTo("_id", yetAnotherTaskId).findFirst()!!
                    innerYetAnotherTask.deleteFromRealm()
                }

                realm.close()
                
                val mongoClient : MongoClient = user.getMongoClient("mongodb-atlas")!!
                val mongoDatabase : MongoDatabase? = mongoClient?.getDatabase("inventory")!!
                val mongoCollection : MongoCollection<Document>? = mongoDatabase?.getCollection("plants")!!

                val watcher : RealmEventStreamAsyncTask<Document>? = mongoCollection?.watchWithFilterAsync(
                    Document("operationType", "delete")
                )

                // BsonInt32(BaseChangeEvent.OperationType.INSERT.ordinal)

                watcher?.get {
                    if (it.isSuccess) {
                        Log.v(
                            "EXAMPLE",
                            "Event type: ${it.get().operationType}, full document: ${it.get().toBsonDocument()}"
                        )
                    } else {
                        Log.e("EXAMPLE", "failed to subscribe to filtered changes in the collection with: ${it.error}")
                    }
                }

                val plant : Document = Document("name", "lily of the valley")
                    .append("sunlight", "full")
                    .append("color", "white")
                    .append("type", "perennial")
                    .append("_partition", "Store 47")
                mongoCollection?.insertOne(plant)?.getAsync() {
                    if (it.isSuccess) {
                        Log.v("EXAMPLE", "successfully inserted a document with id: ${it.get().insertedId}")
                    } else {
                        Log.e("EXAMPLE", "failed to insert documents with: ${it.error}")
                    }
                }

                val plants : List<Document> = Arrays.asList(
                    Document("name", "rhubarb")
                        .append("sunlight", "full")
                        .append("color", "red")
                        .append("type", "perennial")
                        .append("_partition", "Store 47"),
                    Document("name", "wisteria lilac")
                        .append("sunlight", "partial")
                        .append("color", "purple")
                        .append("type", "perennial")
                        .append("_partition", "Store 42"),
                    Document("name", "daffodil")
                        .append("sunlight", "full")
                        .append("color", "yellow")
                        .append("type", "perennial")
                        .append("_partition", "Store 42"))

                mongoCollection?.insertMany(plants)?.getAsync {
                    if (it.isSuccess) {
                        Log.v("EXAMPLE", "successfully inserted ${it.get().insertedIds.size} documents into the collection.")
                    } else {
                        Log.e("EXAMPLE", "failed to insert documents with: ${it.error}")
                    }
                }

                val queryFilter : Document = Document("_partition", "Store 42")
                mongoCollection?.findOne(queryFilter)?.getAsync {
                    if (it.isSuccess) {
                        Log.v("EXAMPLE", "successfully found a document: ${it.get()}")
                    } else {
                        Log.e("EXAMPLE", "failed to find document with: ${it.error}")
                    }
                }

                val findIterable : FindIterable<Document>? = mongoCollection?.find(queryFilter)

                findIterable?.iterator()?.getAsync {
                    if (it.isSuccess) {
                        Log.v("EXAMPLE", "successfully found all plants for Store 42:")
                        it.get().forEach {
                            Log.v("EXAMPLE", it.toString())
                        }
                    } else {
                        Log.e("EXAMPLE", "failed to find documents with: ${it.error}")
                    }
                }

                mongoCollection?.count()?.getAsync {
                    if (it.isSuccess) {
                        Log.v("EXAMPLE", "successfully counted, number of documents in the collection: ${it.get()}")
                    } else {
                        Log.e("EXAMPLE", "failed to count documents with: ${it.error}")
                    }
                }

                val updateDocument : Document = Document("sunlight", "partial")

                mongoCollection?.updateOne(queryFilter, updateDocument)?.getAsync {
                    if (it.isSuccess) {
                        val count : Long = it.get().modifiedCount
                        if (count == 1L) {
                            Log.v("EXAMPLE", "successfully updated a document.")
                        } else {
                            Log.v("EXAMPLE", "did not update a document.")
                        }
                    } else {
                        Log.e("EXAMPLE", "failed to update a document with: ${it.error}")
                    }
                }

                mongoCollection?.updateMany(queryFilter, updateDocument)?.getAsync {
                    if (it.isSuccess) {
                        val count : Long = it.get().modifiedCount
                        if (count == 1L) {
                            Log.v("EXAMPLE", "successfully updated ${count} documents.")
                        } else {
                            Log.v("EXAMPLE", "did not update any documents.")
                        }
                    } else {
                        Log.e("EXAMPLE", "failed to update documents with: ${it.error}")
                    }
                }

                val updateOptions = UpdateOptions().upsert(true)
                mongoCollection?.updateOne(queryFilter, updateDocument, updateOptions)?.getAsync {
                    if (it.isSuccess) {
                        if (it.get().upsertedId != null) {
                            Log.v("EXAMPLE", "successfully upserted a document with id: ${it.get().upsertedId}")
                        } else {
                            Log.v("EXAMPLE", "successfully updated a document.")
                        }
                    } else {
                        Log.e("EXAMPLE", "failed to update or insert document with: ${it.error}")
                    }
                }

                mongoCollection?.deleteOne(queryFilter)?.getAsync {
                    if (it.isSuccess) {
                        val count : Long = it.get().deletedCount
                        if (count == 1L) {
                            Log.v("EXAMPLE", "successfully deleted a document.")
                        } else {
                            Log.v("EXAMPLE", "did not delete a document.")
                        }
                    } else {
                        Log.e("EXAMPLE", "failed to delete document with: ${it.error}")
                    }
                }

                mongoCollection?.deleteMany(queryFilter)?.getAsync {
                    if (it.isSuccess) {
                        val count : Long = it.get().deletedCount
                        if (count != 0L) {
                            Log.v("EXAMPLE", "succcessfully deleted ${count} documents.")
                        } else {
                            Log.v("EXAMPLE", "did not delete any documents.")
                        }
                    } else {
                        Log.e("EXAMPLE", "failed to delete documents with: ${it.error}")
                    }
                }

                val plants2 : List<Document> = Arrays.asList(
                    Document("name", "triffid")
                        .append("sunlight", "low")
                        .append("color", "green")
                        .append("type", "perennial")
                        .append("_partition", "Store 47"),
                    Document("name", "Venomous Tentacula")
                        .append("sunlight", "low")
                        .append("color", "brown")
                        .append("type", "annual")
                        .append("_partition", "Store 42"))

                mongoCollection?.insertMany(plants2)?.getAsync {
                    if (it.isSuccess) {
                        Log.v("EXAMPLE", "successfully inserted ${it.get().insertedIds.size} documents into the collection.")
                    } else {
                        Log.e("EXAMPLE", "failed to insert documents with: ${it.error}")
                    }
                }
            } else {
                Log.e("QUICKSTART", "Failed to log in. Error: ${it.error}")
            }
        }

    }
}
