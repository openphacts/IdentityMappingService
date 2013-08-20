Identity Mapping Service.

This repository depends on 

BridgeDB: https://github.com/openphacts/BridgeDb
Validator https://github.com/openphacts/Validator

Currently neither is available in a maven repository so please download and build these first.

This project depends on configurations as described in BOTH:
{$bridgeDB}README.txt which covers things like setting up MYSQL, DataSources, Transitive directories and Lens.
{$validator}README.md which covers things like RDF setup, and if required external account setup.

The IMS WebService includes all methods from both the BridgeDb WS (including URI calls) and the Validator WS.

===
Properties File Location:
See: {$bridgeDB}README.txt Properties File Location:

==
Data loading:

New to OpenPhacts 1.2: Data loading is done from URIs so no need to have local copies of the files.
Transative linksets are however saved to and loaded from local files.

New to OpenPhacts 1.3: Data loading instructions is instructions come from a xml file.

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
               Will exclude files read earlier by a void or linkset command.
               Excludes some non RDF files but again only does required by OPS implemented.
<doTransitive/> : Tells the loader to compute the transitive linksets
               Typically the last child but this is not required.
               May appear more than once.
               However any linksets / directories in later children are not included.
 
 