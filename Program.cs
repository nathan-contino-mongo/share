using System.Collections.Generic;
using System.IO;

using MongoDB.Bson;
using MongoDB.Driver;
using System;
using System.Threading.Tasks;

using System.Security.Cryptography.X509Certificates;

// dependencies:
// <PackageReference Include="MongoDB.Driver" Version="2.9.2" />
// <TargetFramework>netcoreapp2.2</TargetFramework>

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
            var connectionString = "mongodb+srv://balboaclusterm10-5sfvt.mongodb.net/test?retryWrites=true&w=majority&authMechanism=MONGODB-X509";
            var settings = MongoClientSettings.FromConnectionString(connectionString);

            // You will need to convert your Atlas-provided PEM containing the cert/private keys into a PFX
            // use openssl and the following line to create a PFX from your PEM:
            // openssl pkcs12 -export -in <x509>.pem -inkey <x509>.pem -out <x509>.pfx -certfile <x509>.pem
            // and provide a password, which should match the second argument you pass to X509Certificate2
            var cert = new X509Certificate2("x509-temp.pfx", "password");

            settings.SslSettings = new SslSettings {
                ClientCertificates = new List<X509Certificate>()
                {
                    cert
                }
            };

            var client = new MongoClient(settings);
            
            // just doing a quick read to verify the usability of this connection
            var database = client.GetDatabase("testDB");
            var collection = database.GetCollection<BsonDocument>("testCol");
            
            var docCount = collection.CountDocuments("{}");
            Console.WriteLine(docCount);
        }
    }
}
