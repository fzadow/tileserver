
This is the TileBuilder tile server. It serves image tiles for [CATMAID](https://github.com/catmaid/CATMAID). TileBuilder is a self-contained Java application that launches a web server on port 8000. TileBuilder reads images in a specialized HDF5 format which can be generated with the [Fiji plugin from the /scripts directory](https://github.com/fzadow/tileserver/tree/master/scripts).

# Simple Installation

* Make sure Java 8 is installed on your server.
* Download the [WAR file from the /target directory](https://github.com/fzadow/tileserver/raw/master/target/tilebuilder.war) to your server, for example by using the wget command

  `wget https://github.com/fzadow/tileserver/raw/master/target/tilebuilder.war`.
* create `/opt/etc/tileserver/config.properties` or edit the `config.properties` within the WAR.
* customize config.properties (see https://github.com/fzadow/tileserver/blob/master/config.properties):
	* source_image_dir
	* tile_dir
	* writable_tile_dir
* run with `java -jar tilebuilder.war`

# Command line options

These command line options can be used when running tilebuilder:

* `--server.port=9000` runs Tilebuilder on port 9000 instead of the default (8080)

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
