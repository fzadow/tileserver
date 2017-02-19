
This is the TileBuilder tile server. It serves image tiles for [CATMAID](https://github.com/catmaid/CATMAID). TileBuilder is a self-contained Java application that launches a web server on a configurable port (default: 8080). TileBuilder reads images in a specialized HDF5 format which can be generated with the [Fiji plugin from the /scripts directory](https://github.com/fzadow/tileserver/tree/master/scripts).

TileBuilder has been tested to run with Java 7 and 8.

# Simple Installation (using the pre-built TileBuilder.war)

* Create a directory that you want to run TileBuilder from on your web server and change to that directory. For example, go to your home (`~`) directory and create the directory "tilebuilder":

```
cd ~
mkdir tilebuilder
cd tilebuilder
```

* Within the directory you created, create another directory that will later hold the images TileBuilder will serve.

```
mkdir images
```


* Download the [WAR file from the /target directory](https://github.com/fzadow/tileserver/raw/master/target/tilebuilder.war) and the [sample configuration file](https://github.com/fzadow/tileserver/raw/master/config.properties) to your server, for example by using these commands

```
wget https://github.com/fzadow/tileserver/raw/master/target/tilebuilder.war
wget https://github.com/fzadow/tileserver/raw/master/config.properties
```

* customize the file config.properties you just downloaded:
	* source_image_dir
	* tile_dir
	* writable_tile_dir

* run TileBuilder `java -jar tilebuilder.war`

## Command line options

These command line options can be used when running TileBuilder:

* `--server.port=9000` runs Tilebuilder on port 9000 instead of the default (8080)
* `-Dlevel=DEBUG` to enable debug output



# Usage

TileBuilder responds to tile requests via HTTP with the syntax for TileSource type 9, [described in detail in the CATMAID documentation](http://catmaid.readthedocs.io/en/stable/tile_sources.html):

```
http://tileserver:8000/imagename/stackname/s/r_c_z.jpg
```

Here, `imagename` identifies the HDF5 file containing the image data (imagename.h5) and its associated description file (imagename.yaml),
`stackname` identifies the name of the HDF5 Group within the HDF5 file,
`s` is the slice index,
`r` is the row (y-coordinate),
`c` is the column (x-coordinate),
`z` is the zoom level.

To display an image with TileBuilder in CATMAID, import it via the [Importer admin tool](http://catmaid.readthedocs.io/en/stable/importing_data.html) in CATMAID.

# Compilation

Maven 3 is used to manage dependencies and compile the project. To create a new
WAR file, use the following command: `mvn package`. The generated
`tileserver.war` file can be found in the `target` folder.
