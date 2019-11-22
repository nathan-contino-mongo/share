using System.Collections.Generic;
using System.IO;

using MongoDB.Bson;
using MongoDB.Driver;
using System;
using System.Threading.Tasks;

// Tested using MongoDB.Driver 2.9.2 and netcoreapp 2.2

namespace WorkingWithMongoDB
{
    class Program
    {
        static void Main(string[] args)
        {
            MainAsync().Wait();
        }

        static async Task MainAsync()
        {
            // note that you can specify the username and password here or use these as placeholders and override with the CreateCredential below
            var connectionString = "mongodb+srv://username:password@balboaclusterm10-5sfvt.mongodb.net/test?retryWrites=true&w=majority";
            var settings = MongoClientSettings.FromConnectionString(connectionString);

            settings.Credential =  MongoCredential.CreateCredential("admin", "admin", "admin");

            var client = new MongoClient(settings);
            
            // just doing a quick read to verify the usability of this connection
            var database = client.GetDatabase("testDB");
            var collection = database.GetCollection<BsonDocument>("testCol");
            
            var docCount = collection.CountDocuments("{}");
            Console.WriteLine(docCount);
        }
    }
}
