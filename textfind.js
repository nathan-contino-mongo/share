// ignored first line
const { MongoClient } = require("mongodb");

// Replace the following with your MongoDB deployment's connection
// string.
const uri =
  "mongodb+srv://admin:admin@mflix-2sp0m.mongodb.net/test?retryWrites=true&w=majority";

const client = new MongoClient(uri, {
  useNewUrlParser: true,
  useUnifiedTopology: true,
});

async function run() {
  try {
    await client.connect();

    const database = client.db("sample_mflix");
    const collection = database.collection("users");

    const query = { $text: { $search: "stark" } };

    // sort returned documents by descending text relevence score
    const sort = { score: { $meta: "textScore" } };
    // Include only the `title` and `imdb` fields in each returned document
    const projection = {
      _id: 0,
      name: 1,
      email: 1,
      score: { $meta: "textScore" },
    };

    // find documents based on our query, sort, and projection
    const cursor = collection
      .find(query)
      .sort(sort)
      .project(projection);

    //uncomment these two lines to see strange behavior:
    //const docs = await cursor.toArray();
    //console.log(docs);
    
    // print a message if no documents were found
    if (!(await cursor.hasNext())) {
      console.log("No documents found!");
    }
    await cursor.forEach(console.dir);
  } finally {
    await client.close();
  }
}
run().catch(console.dir);
