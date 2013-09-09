Identity Mapping Service.

This repository depends on 

BridgeDB: https://github.com/openphacts/BridgeDb
Validator https://github.com/openphacts/Validator

Currently neither is available in a maven repository so please download and build these first.

Please see the {$bridgeDB}README.md for documentation including the property files it refers to.
Note: The Transitive section of config.txt is required by the IMS

The IMS WebService includes all methods from both the BridgeDb WS (including URI calls) and the Validator WS.

===
Configuration:

This project depends on configurations as described in BOTH:
{$bridgeDB}README.md which covers things like setting up MYSQL, DataSources, Transitive directories and Lens.
{$validator}README.md which covers things like RDF setup, and if required external account setup.

----
local.properties
-
A single local.properties file is shared between BridgeDB, Validator, IMS and if applicable QueryExpander

-
Finding local copied of URIs being loaded.

In addition to overwriting any value in any other properties file, local.properties can also be used to set URI pattern to file path mappings.

The path to file properties map a uriPattern to a file path.
So if there is a local copy of the data the file is used.
For each URI that begins with a known pattern the URI String is changed replacing the uriPattern with the path.
The new String is used to try to find a local copy of the URI.
If a local copy is found, and is readable, this file is passed to the OpenRDF parser.
No attempt will be made to see if the local file is the same as the one at the URI!

If no local copy is found the URI will be copied to a temporary file which is removed after loading.
The WS will always publish the URI and NOT the replacement file

The property key is in 3 parts separated by a full stop.

Part 1 is always "pathToFile". This allows these properties to be mixed with others in local.properties

Part 2 Is used purely to map a uriPattern to a path: 
    It can be any String legal in Java properties keys without a fullstop in it.

Part 3 uriPattern
    Beginning of a URI that is to be replaced with a path.
    Does not need to go all the way to the last / as long as the local directories are Exactly the same as the URI directories
    As the pattern (value of this property) is the key to a map they must all be UNIQUE! 
        IE. You can not use the same pattern twice. But you can use one pattern that is a substring of another.
        
Part 3 path
    The path to the local copies of the file.
    This must be in the local file format.
    Remember to put the slashes in the direction used by the local system.
         \ for windows   \ for linux
    If and only if the uriPattern ends with a slash the path must too.
    Remember the loader must have the correct permission to read the file.
    The Loader will NOT write to this path. (so write permissions not required)
    
example:
pathToFile.ops.uriPattern    http://openphacts.cs.man.ac.uk/ims/linkset/
pathToFile.ops.path          /OPS/linksets/

While the uriPatterns must be unique. Alternatives can be given by shortening both the uriPattern and path
For example: (works together with the previous example)
pathToFile.opsMore.uriPattern    http://openphacts.cs.man.ac.uk/ims/linkset
pathToFile.opsMore.path          /OPS_more/linksets

Note: The system will automatically create a mapping between TransitiveDirectory and TransitiveBaseUri (see BridgeDB properties)

==
Data loading:

New to OpenPhacts 1.2: Data loading is done from URIs so no need to have local copies of the files.
New to OpenPhacts 1.3: Data loading instructions is instructions come from a xml file.

Transitive linksets are however saved to and loaded from local files.

To Load run loader-*.one-jar (using java -jar)
Found in the loader module.
This will run the main in uk.ac.manchester.cs.openphacts.ims.loader.RunLoader

RunLoader takes an Optional parameter of the URI to an xml file that shows what to load.
It will have a default value that points to the current suggested OpenPhacts load.

Note: best is to copy your configuration files into the folder that holds the jar.
And make sure there are no RELATIVE directories!

=====
load.xml 

The URI parameter to RunLoader should point to an xml file often called load.xml

This will have the following elements directly as children of the root (typcially <loadSteps>)
<clearAll/>  : Tells the loader to clear any existing data SQL and RDF and start from fresh
               Must be the first child. Otherwise all loading up to this point will be lost!
<recover/>   : Tells the loader to attempt to restart from a previous loaded that failed part way through.
               The load.xml should not include any URIs successfully loaded.
               Must be the first child.
<void>       : Tells the loader to load a URI as a void file
               Format <void>URI</void>
               Only required if a URI used in a void:inDataSet, or void:subset statement is not resolvable.
               Not required where the URI used is resolvable and readable. 
                    (Can depend on Username and password is know) see {$validator}README.md
               Must be an earlier child than the linksets that depend on this void.
               The URI must end with the file extension that matches the RDF format.
               Can be a file URI.
<linkset>    : Tells the loader to load a URI as a void file
               Format <linkset>URi</linkset>
               The URI must end with the file extension that matches the RDF format.
               Can be a file URI.
               The URI may include void statements but MUST include links.
<directory>  : Tells the loader to load all files in this URi directory (including sub directories.
               Format <directory>URI</directory>
               Can only be a URI to a directory that returns a HTML page showing the files
               Limited implementation that works on the Manchester OPS server. Untested elsewhere
               Not expected to work with a file URI.
               Therefor pathToFile settings are ignored when reading directories. But are used for the files in the directory.
               Will exclude files read earlier by a void or linkset command.
               Excludes some non RDF files but again only does required by OPS implemented.
<doTransitive/> : Tells the loader to compute the transitive linksets
               Typically the last child but this is not required.
               May appear more than once.
               However any linksets / directories in later children are not included.
 
 