// ignored first line
const { MongoClient } = require("mongodb");
const fs = require("fs");

const uri =
  "mongodb+srv://admin:admin@mflix-2sp0m.mongodb.net/test?retryWrites=true&w=majority";

const client = new MongoClient(uri, {
  useNewUrlParser: true,
  useUnifiedTopology: true,
});

async function run() {
  try {
    await client.connect();

    const mflix = client.db("sample_mflix");

    // Replace with the path to your copy of the users.json file
    var users = JSON.parse(fs.readFileSync("../../data/users.json"));

    var usersToInsert = [];

    // Loop through each user in the users array and format the data from
    // the JSON file to be a series of insertOne operations. Append
    // each appropriately formatted object to the usersToInsert array.
    users.forEach(user => {
      usersToInsert.push({ insertOne: { document: user } });
    });

    // Perform the bulk write operation and print out the resulting
    // BulkWriteResult object. This is a routine data import
    // that does not rely on the ordering of the operations to ensure
    // data consistency, so you can set the ordered option to false
    // to improve write performance.
    mflix
      .collection("users")
      .bulkWrite(usersToInsert, { ordered: false }, function(error, result) {
        if (error) {
          console.log("Error: " + error);
        } else {
          if (result.nInserted > 0) {
            console.log("Number of documents inserted: " + result.nInserted);
          } else {
            console.log(
              "No documents were inserted during the bulk write operation",
            );
          }
        }
      });
  } finally {
    console.log("closing");
    await client.close();
    console.log("closed");
  }
}
run().catch(console.dir);
