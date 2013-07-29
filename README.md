Identity Mapping Service.

This repository depends on 

BridgeDB: https://github.com/openphacts/BridgeDb
Validator https://github.com/openphacts/Validator

Currently neither is available in a maven repository so please download and build these first.

Please see the {$bridgeDB}README.md for documentation including the config.txt it refers to.
Note: The Transitive section of config.txt is required by the IMS

The IMS WebService includes all methods from both the BridgeDb WS (including URI calls) and the Validator WS.

==
Data loading:
New to OpenPhacts 1.2: Data loading is done from URIs so no need to have local copies of the files.
Transitive linksets are however saved to and loaded from local files.
So the Transitive section of config.txt is critical here!

To Load run loader-*.one-jar (using java -jar)
Found in the loader module.
This will run the main in uk.ac.manchester.cs.openphacts.ims.loader.RunLoader

Note: best is to copy your config.txt into the folder that holds the jar.
And make sure there are no RELATIVE directories!





 
 