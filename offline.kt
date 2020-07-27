package com.mongodb.realmsnippets

import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.bson.types.ObjectId

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import io.realm.*

import io.realm.kotlin.where
/*import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration

import io.realm.mongodb.Credentials
import io.realm.mongodb.User
import io.realm.mongodb.sync.SyncConfiguration*/


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Realm.init(this) // context, usually an Activity or Application
        val appID = "authsample-pdvvn" //"<your app ID>" // replace this with your App ID
        //val app: App = App(AppConfiguration.Builder(appID)
         //   .build())

        //val credentials: Credentials = Credentials.anonymous()

        //var user: User? = null

        //app.loginAsync(credentials) {
            //if (it.isSuccess) {
                Log.v("QUICKSTART", "Successfully authenticated anonymously.")
                //user = app.currentUser()

                val partitionValue: String = "myPartition"

                val config = RealmConfiguration.Builder().build()
                //val config = SyncConfiguration.Builder(user!!, partitionValue)
                    //.waitForInitialRemoteData()
                //    .build()

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


                /*user?.logOutAsync {
                    if (it.isSuccess) {
                        Log.v("QUICKSTART", "Successfully logged out.")
                    } else {
                        Log.e("QUICKSTART", it.error.toString())
                    }
                }
            } else {
                Log.e("QUICKSTART", "an error occurred:")
                Log.e("QUICKSTART", it.error.toString())
            }*/
        //}



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

/*
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.User
import io.realm.mongodb.functions.Functions
import io.realm.mongodb.mongo.MongoClient
import io.realm.mongodb.mongo.MongoCollection
import io.realm.mongodb.mongo.MongoDatabase
import io.realm.mongodb.mongo.iterable.AggregateIterable
import io.realm.mongodb.mongo.iterable.FindIterable
import io.realm.mongodb.mongo.iterable.MongoCursor
import io.realm.mongodb.mongo.options.UpdateOptions
import org.bson.Document
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var realmApp: App
    private var mongoClient: MongoClient? = null
    private lateinit var itemsCollection: MongoCollection<Document>
    private lateinit var user: User

    //val appID = "inventory-demo-guiqc"
    //val realmURL = "https://realm-dev.mongodb.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val TAG = "REALMSNIPPETS"
/*
        val appID : String = "<your app ID>"; // replace this with your App ID
        Realm.init(this); // initialize Realm, required before interacting with SDK
        realmApp = App(
            AppConfiguration.Builder(appID)
            .build());

        // an authenticated user is required to access a MongoDB instance
        val credentials : Credentials = Credentials.anonymous();
        realmApp.loginAsync(credentials) {
            if (it.isSuccess) {
                user = realmApp.currentUser()!!;
                mongoClient = user.getMongoClient("<atlas service name>");
                if (mongoClient != null) {
                    itemsCollection = mongoClient?.getDatabase("<database name>")
                        .getCollection("<collection name>");
                    Log.v(TAG, "Successfully connected to the MongoDB instance.");
                } else {
                    Log.e(TAG, "Error connecting to the MongoDB instance.");
                }
            }
            else {
                Log.e(TAG, "Error logging into the Realm app. Is anonymous authentication enabled?");
            }
        }*/


        val appID = "authsample-pdvvn"
        val realmURL = "https://realm.mongodb.com"

        Realm.init(this)
        val app: App = App(AppConfiguration.Builder(appID)
                .build())

        val anonymousCredentials: Credentials = Credentials.anonymous()
        app.loginAsync(anonymousCredentials) {
            if (it.isSuccess) {
                val user: User? = app.currentUser()

                val functionsManager: Functions = app.getFunctions(user)
                val args: List<Int> = listOf(1, 2)
                functionsManager.callFunctionAsync("sum", args, Integer::class.java) { result ->
                    run {
                        if (result.isSuccess) {
                            Log.v(TAG, "Sum value: ${result.get()}")
                        } else {
                            Log.e("EXAMPLE", "failed to call sum function with: " + result.error)
                        }
                    }
                }
            } else {
                Log.e("EXAMPLE", "Error logging into the Realm app. Make sure that anonymous authentication is enabled. Error: " + it.error)
            }
        }


        /*val email = "steve"
        val password = "password"

        app.emailPasswordAuth.registerUserAsync(email, password) {
            if (it.isSuccess) {
                Log.i(TAG,"Successfully registered user.")
            } else {
                Log.e(TAG,"Failed to register user: ${it.error}")
            }
        }

        val token = "token"
        val tokenId = "tokenId"

        // token and tokenId are query parameters in the confirmation
        // link sent in the confirmation email.
        app.emailPasswordAuth.confirmUserAsync(token, tokenId) {
            if (it.isSuccess) {
                Log.i(TAG, "Successfully confirmed new user.")
            } else {
                Log.e(TAG, "Failed to register user: ${it.error}")
            }
        }

        val newPassword = "newPassword"

        val emailAddress = "nchewbacca@gmail.com"

        app.emailPasswordAuth.sendResetPasswordEmailAsync(emailAddress) {
            if (it.isSuccess) {
                Log.i(TAG, "Successfully sent the user a reset password link to $emailAddress")
            } else {
                Log.e(TAG, "Failed to send the user a reset password link to $emailAddress")
            }
        }

        // token and tokenId are query parameters in the confirmation
        // link sent in the password reset email.
        app.emailPasswordAuth.resetPasswordAsync(token, tokenId, newPassword) {
            if (it.isSuccess) {
                Log.i(TAG, "Successfully updated password for user.")
            } else {
                Log.e(TAG, "Failed to reset user's password.")
            }
        }

        val username = "natedawg95@gmail.com"*/

        //val googleCredentials: Credentials = Credentials.google("test")
        //val apiCredentials: Credentials = Credentials.apiKey("EGYQkm6EDjjhfJna93L867UcLmRuZX0e7iQKvgx7nDi2fUoLrlIwYoqeID07VKJY");
        val emailPasswordCredentials : Credentials = Credentials.emailPassword("steve", "password");
        //val customFunctionCredentials: Credentials = Credentials.customFunction("authFunction", "arg1", "arg2", 3)

        //val customJWTCredentials: Credentials = Credentials.jwt("customToken&&&&&&&&&&&&&&&&&&&&&&&&&")

        //val googleOAuthCredentials: Credentials = Credentials.google("googleToken")

        //val facebookOAuthCredentials: Credentials = Credentials.facebook("facebookToken")

        //val signInWithAppleCredentials: Credentials = Credentials.apple("appleIDToken")

        //val anonymousCredentials: Credentials = Credentials.anonymous()

        app.loginAsync(emailPasswordCredentials) {
            if (it.isSuccess) {
                /*
                Log.v("QUICKSTART", it.get().email);
                if (app.currentUser() == it.get()) {
                    Log.v(TAG,"Current user is joe")
                } else {
                    Log.e(TAG, "Current user is not joe")
                }*/
                val user: User? = app.currentUser()

                val mongoClient : MongoClient? = user?.getMongoClient("mongodb-atlas")
                val mongoDatabase : MongoDatabase? = mongoClient?.getDatabase("inventory")
                val mongoCollection : MongoCollection<Document>? = mongoDatabase?.getCollection("plants")

                val pipeline : List<Document> = Arrays.asList(
                    Document("\$group", Document("_id", "\$type")
                        .append("totalCount", Document("\$sum", 1))))
                val aggregationIterator : AggregateIterable<Document>? =
                    mongoCollection?.aggregate(pipeline)

                aggregationIterator?.iterator()?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.v("EXAMPLE", "Plant type counts: ")
                        it.result.forEach {
                            Log.v("EXAMPLE", it.toString())
                        }
                    } else {
                        Log.e("EXAMPLE", "failed to aggregate documents with: ${it.exception}")
                    }
                }


                /*val user: User? = app.currentUser()

                val mongoClient : MongoClient? = user?.getMongoClient("mongodb-atlas")

                val mongoDatabase : MongoDatabase? = mongoClient?.getDatabase("TaskTracker")

                val mongoCollection : MongoCollection<Document>? = mongoDatabase?.getCollection("Task") //, Task::class.java)

                val queryFilter : Document = Document("color", "green")
                mongoCollection?.deleteOne(queryFilter)?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        val count : Long = it.result.deletedCount
                        if (count == 1L) {
                            Log.v("EXAMPLE", "successfully deleted a document.")
                        } else {
                            Log.v("EXAMPLE", "did not delete a document.")
                        }
                    } else {
                        Log.e("EXAMPLE", "failed to delete document with: ${it.exception}")
                    }
                }*/
                /*
                val queryFilter : Document = Document("sunlight", "full")
                    .append("type", "perennial")
                    .append("color", "green")
                    .append("_partition", "Store 47")
                val updateDocument : Document = Document("name", "sweet basil")
                val updateOptions : UpdateOptions = UpdateOptions().upsert(true)
                mongoCollection?.updateOne(queryFilter, updateDocument, updateOptions)?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (it.result.upsertedId != null) {
                            Log.v("EXAMPLE", "successfully upserted a document with id: ${it.result.upsertedId}")
                        } else {
                            Log.v("EXAMPLE", "successfully updated a document.")
                        }
                    } else {
                        Log.e("EXAMPLE", "failed to delete document with: ${it.exception}")
                    }
                }*/
/*
                val result : com.google.android.gms.tasks.Task<Document>? = mongoCollection?.findOne()

                result?.addOnCompleteListener {
                    Log.e(TAG, it.result.toString())
                }*/

                /*val plant : Document = Document("name", "lily of the valley").append("sunlight", "full").append("color", "white").append("type", "perennial").append("_partition", "My Project")

                mongoCollection?.insertOne(plant)?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.e(TAG, "inserted id: " + it.result.insertedId)
                    } else {
                        Log.e("EXAMPLE", "failed with : ${it.exception}")
                    }
                }*/
/*
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

                mongoCollection?.insertMany(plants)?.addOnCompleteListener {
                    Log.e(TAG, "inserted ids size: " + it.result.insertedIds.size)
                }

                val findOne: Task<Document>? = mongoCollection?.findOne(Document("name", "never ending story"))

                findOne?.addOnCompleteListener {
                    Log.e(TAG, "${it.result}")
                }

                val findResult : FindIterable<Document>? = mongoCollection?.find(Document("_partition", "My Project"))

                //findResult?.iterator()?.result?.forEach {
                //    Log.e(TAG, "find result? : " + it);
                //}

                val findResultIterable : Task<MongoCursor<Document>>? = findResult?.iterator()

                findResultIterable?.addOnCompleteListener { it ->
                    it.result.forEach {
                        Log.e(TAG, it.toString());
                    }
                }

                mongoCollection?.count()?.addOnCompleteListener {
                    Log.e(TAG, "Count of docs in collection: " + it.result)
                }

                val queryFilter : Document = Document("name", "never ending story")
                val updateDocument : Document = Document("status", "Complete")
                val updateOptions : UpdateOptions = UpdateOptions().upsert(true)
                mongoCollection?.updateOne(queryFilter, updateDocument)?.addOnCompleteListener {
                    Log.e(TAG, "Updated ${it.result.modifiedCount} document.")
                }

                mongoCollection?.updateOne(Document("foo", "bar"), Document("fizz", "buzz"), UpdateOptions().upsert(true))?.addOnCompleteListener {
                    Log.e(TAG, "Upserted ${it.result.upsertedId} document.")
                }

                mongoCollection?.updateMany(Document("name", "never ending story"), Document("status", "Complete"))?.addOnCompleteListener {
                    Log.e(TAG, "Updated ${it.result.modifiedCount} documents.")
                }

                mongoCollection?.deleteOne(Document("name", "never ending story"))?.addOnCompleteListener {
                    Log.e(TAG, "Deleted ${it.result.deletedCount} document.")
                }

                mongoCollection?.deleteMany(Document("name", "never ending story"))?.addOnCompleteListener {
                    Log.e(TAG, "Deleted ${it.result.deletedCount} documents.")
                }
*/
                /*
                val aggregationResultIterable : Task<MongoCursor<Document>>? =
                    mongoCollection?.aggregate(
                        Arrays.asList(Document("\$group", Document("_id", "\$type").append("totalCount", Document("\$sum", 1)))))?.iterator()

                aggregationResultIterable?.addOnCompleteListener {
                    it.result.forEach {
                        Log.e(TAG, "agg pipe: " + it)
                    }
                }


                user?.apiKeyAuth?.disableApiKeyAsync(ObjectId()) {
                    if (it.isSuccess) {
                        Log.v(TAG, "Successfully disabled the API Key: " + it.get())
                    } else {
                        Log.e(TAG, "Error disabling API key: ", it.error)
                    }
                }

                //user?.apiKeyAuth?.deleteApiKeyAsync()

                user?.apiKeyAuth?.createApiKeyAsync("<name-of-the-api-key>") {
                    if (it.isSuccess) {
                        Log.v(TAG, "Successfully created the API Key: " + it.get().value)
                    } else {
                        Log.e(TAG, "Error creating API key: ", it.error)
                    }
                }

                user?.apiKeyAuth?.fetchAllApiKeys() {
                    if (it.isSuccess) {
                        Log.v(TAG, "Successfully fetched API keys: " + Arrays.toString(it.get().toTypedArray()))
                    } else {
                        Log.e(TAG, "Error fetching API keys: " + it.error)
                    }
                }


                user?.apiKeyAuth?.createApiKeyAsync("arggggggg") {
                    if (it.isSuccess) {
                        user?.apiKeyAuth?.fetchApiKeyAsync(it.get().id) {
                            if (it.isSuccess) {
                                Log.e(TAG, it.get()?.name)
                            } else {
                                Log.e(TAG, "uh OH no fetchy" + it.error)
                            }
                        }
                    } else {
                        Log.e(TAG, "uh OH no create" + it.error)
                    }

                }*/
/*
                val functionsManager: Functions = app.getFunctions(user)
                val args: ArrayList<Int> = ArrayList<Int>()
                args.add(1)
                args.add(2)
                functionsManager.callFunctionAsync("sum", args, Integer::class.java) {
                        result -> Log.v(TAG, "Sum value: ${result.get()}")
                }
                Log.v(TAG, "called function, waiting for result")

                // configure realm to use the current user and the partition corresponding to "My Project"
                val config = SyncConfiguration.Builder(user!!, "All Stores")
                    .waitForInitialRemoteData()
                    .build()

                // save this configuration as the default for this entire app so other activities and threads can open their own realm instances
                Realm.setDefaultConfiguration(config)

                lateinit var realm: Realm
                // Sync all realm changes via a new instance, and when that instance has been successfully created connect it to an on-screen list (a recycler view)
                /*Realm.getInstanceAsync(config, object: Realm.Callback() {
                    override fun onSuccess(_realm: Realm) {
                        // since this realm should live exactly as long as this activity, assign the realm to a member variable
                        realm = _realm
                        val tasks : RealmResults<Task>? = realm.where<Task>().findAll()

                        val task : Task? = realm.where<Task>().findFirst()

                        tasks?.addChangeListener {tasks, changeSet
                            -> Log.e(TAG,"Update to tasks collection:" + changeSet.changes)
                        }

                        // add listener to realm
                        realm.addChangeListener {
                            Log.e(TAG, "update to realm! ")
                        };

                        task?.addChangeListener<Task> {task, changeSet -> Log.e(TAG, "update to task: " + changeSet?.changedFields?.get(0))}

                        realm.executeTransaction {
                            task?.name = "new name"
                        }

                        // make change happen -> check output for reaction
                        realm.executeTransaction {
                            it.insert(Task())
                        }
                        Log.v(TAG, "Tasks found: ${tasks?.size}")

                        val test: FindIterable<Document>? = user?.getMongoClient("test").getDatabase("test").getCollection("test").find();
/*
                        user.apiKeyAuth.fetchAllApiKeys() {
                            if (it.isSuccess) {
                                Log.e(TAG, "" + it.get().size)
                            } else {
                                Log.e(TAG, "ruh roh no fetchy keys: ${it.error}")
                            }
                        }

                        user.apiKeyAuth.createApiKeyAsync("test") {
                            if(it.isSuccess) {
                                Log.e(TAG, it.get().value)
                            } else {
                                Log.e(TAG, "uh oh: ${it.error}")
                            }
                        }*/
                    }*/ */
               // })



            } else {
               Log.e(TAG, "Error logging in: ${it.error.toString()}")
            }
       }


        // val anonUser: User = app.login(anonymousCredentials);

        /*
        val creds = Credentials.emailPassword(username, password)
        app.loginAsync(creds) {
            if (!it.isSuccess) {
                Log.e(TAG, it.error.toString())
            } else {
                Log.v(TAG, "Successfully authenticated!")
            }
        }*/



//*/

    }
}
*/
