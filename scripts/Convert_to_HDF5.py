from java.awt import Color
from java.awt.event import TextListener
from javax.swing import JButton, JScrollPane, JPanel, JComboBox, JLabel, JFrame, JTextField, JCheckBox, JOptionPane
from java.awt import BorderLayout, Choice, Color, Font, GridLayout, GridBagLayout, GridBagConstraints
from java.awt.event import ActionListener


import os
import time
import math

from ij import Prefs
from ij import IJ, WindowManager
from ij import Menus
from ij.io import DirectoryChooser
from ij.plugin.filter import Info
from ij.gui import GenericDialog

#commands = [c for c in ij.Menus.getCommands().keySet()]
# Above, equivalent list as below:
#commands = Menus.getCommands().keySet().toArray()
#gd = GenericDialog('Command Launcher 2')
#gd.addStringField('Command: ', '');
#prompt = gd.getStringFields().get(0)
#prompt.setForeground(Color.red)

#gd.showDialog()
#if not gd.wasCanceled(): IJ.doCommand(gd.getNextString())

# This python version does not encapsulate the values of the variables, so they are all global when defined outside the class definition.
# In contrast, the lisp 'let' definitions encapsulates them in full
# As an advantage, each python script executes within its own namespace, whereas clojure scripts run all within a unique static interpreter.



class ConvertJob:
	def __init__(self, imp):
		self.logMessages = []
		self.projectName = ""
		self.tileSize = 16

		self.image = imp
		self.width = imp.width
		self.height = imp.height
		self.slices = imp.getNSlices()
		self.channels = imp.getNChannels()
		self.projectName = imp.getShortTitle()

		self.resolution = self.getResolution()
		self.metadata = ''

		self.scaleLevels = -1
		self.scaleLevelsGenerated = False

	def setScaleLevels(self, scaleLevels):
		if not self.image:
			print("ERR: image not set")
			return
		if not self.tileSize:
			print("ERR: tile size not set")
			return

		self.scaleLevelsGenerated = (scaleLevels == 0)
		if self.scaleLevelsGenerated:
			self.scaleLevels = int( math.log( max( self.image.dimensions[0], self.image.dimensions[1] ) / self.tileSize, 2 ) ) + 1
		else:
			self.scaleLevels = scaleLevels
		self.log("Number of scale levels: " + str(self.scaleLevels) + " (tilesize=" + str(self.tileSize) + ")")

	def askForOutputDir(self):
		outputDir = DirectoryChooser("Please select output directory").getDirectory()
		if outputDir:
			self.outputDir = outputDir

	def log(self, message):
		self.logMessages.append(message)
		print(message)
		time.sleep(0.001)

	def validate(self):
		if not self.outputDir:
			raise ValueError("Need output directory")

	def saveLog(self, path):
		f = None
		try:
			f = open(path, 'w')
			for item in self.logMessages:
				f.write("%s\n" % item)
		except:
			print("ERR: Could not save log file!")
		finally:
			if f is not None:
				f.close()

	def saveProperties(self):
		self.pset("projectName", self.projectName)

	def getResolution(self):
		if not self.image:
			raise ValueError('Need image')

		clb = self.image.getCalibration()

		if clb:
			return clb.pixelWidth, clb.pixelHeight, clb.pixelDepth
		else:
			return 1.0, 1.0, 1.0
			
	def saveYaml(self):
		f = None
		path = self.outputPathWithoutExtension + ".yaml"

		try:
			self.log("Saving YAML file")
			f = open( path, 'w')

			template_channels = """
		- folder: "channel%(num)d"
			name: "%(name)s"
			metadata: "%(metadata)s"
			dimension: "(%(dimx)s,%(dimy)s,%(dimz)s)"
			resolution: "(%(resx)s,%(resy)s,%(resz)s)"
			zoomlevels: %(zoomlevels)d"""

			template_composite_channels = """
			- stack: "channel%(num)d"
			  color: "%(color)s\""""

			channels_string = ""

			composite_channels_string = ""
			zoomlevels = -1 if self.scaleLevelsGenerated else self.scaleLevels
			for c in range( 0, self.channels ):
				context_channels = {
					'num' : c,
					'name' : "Channel " + str( c ),
					'metadata' : self.metadata[c],
					'dimx' : self.image.dimensions[0],
					'dimy' : self.image.dimensions[1],
					'dimz' : self.image.dimensions[3],
					'resx' : self.resolution[0],
					'resy' : self.resolution[1],
					'resz' : self.resolution[2],
					'zoomlevels': zoomlevels,
					'color' : self.colors[c]
				}
				channels_string = channels_string + template_channels % context_channels
				composite_channels_string = composite_channels_string + template_composite_channels % context_channels


			# Add composite channel to YAML string

			channels_string = channels_string + """
		- folder: "composite"
			name: "Composite"
			dimension: "(%(dimx)s,%(dimy)s,%(dimz)s)"
			resolution: "(%(resx)s,%(resy)s,%(resz)s)"
			channels:%(composite_channels)s""" % {
					'dimx' : self.image.dimensions[0],
					'dimy' : self.image.dimensions[1],
					'dimz' : self.image.dimensions[3],
					'resx' : self.resolution[0],
					'resy' : self.resolution[1],
					'resz' : self.resolution[2],
					'composite_channels' : composite_channels_string
				}


			# Generate main YAML string

			yaml_string = """project:
	name: "%(name)s"
	stacks:%(channels)s""" % {
				'name' : self.projectName,
				'channels' : channels_string
			}

			f.write( yaml_string )






		except Exception, e:
			print("Error: Could not save YAML file: " + str(e))
		finally:
			if f is not None:
				f.close()
				self.log("Saved YAML file to " + path )

	def saveHDF5(self):
		self.log( "Creating HDF5 file" )
		path = self.outputPathWithoutExtension + ".h5"

		self.imageClone = self.image.clone()

		self.log( "Saving scale level 0 (" + str( self.imageClone.width ) + "x" + str( self.imageClone.height ) + ")." )
		IJ.run( self.imageClone, "Scriptable save HDF5 (new or replace)...", "save=" + path + " dsetnametemplate=/stacks/channel{c}/0 formattime=%d formatchannel=%d compressionlevel=0" )
		scale_level = 1
		while self.imageClone.width > self.tileSize or self.imageClone.height > self.tileSize:
			self.log( "Scale level " + str( scale_level - 1 ) + " saved. Scaling down." )
			IJ.run( self.imageClone, "Bin...", "x=2 y=2 y=1 bin=Average")
			self.log( "Saving scale level " + str( scale_level ) + " (" + str( self.imageClone.width ) + "x" + str( self.imageClone.height ) + ")." )
			IJ.run( self.imageClone, "Scriptable save HDF5 (append)...", "save=" + path + " dsetnametemplate=/stacks/channel{c}/" + str( scale_level ) + " formattime=%d formatchannel=%d compressionlevel=0" )
			scale_level = scale_level + 1

		self.log( "Scale level " + str( scale_level - 1 ) + " saved." )
		self.log( "Smallest size reached. Saved " + str( scale_level ) + " scale levels.")
		self.log( "HDF5 file saved to " + path + "\n" )

		self.imageClone.flush()


def saveIJLog(path):
	f = None
	try:
		f = open(path, 'w')
		logText = IJ.getLog()
		if logText is not None:
			f.write(logText)
	except:
		print("ERR: Could not save IJ log file!")
	finally:
		if f is not None:
			f.close()

def getDataPart( job, data, lineStart ):
	""" Get the end of line in data, starting with lineStart.
	"""
	posOne = data.find( lineStart )
	if posOne != -1:
		posOne = posOne + len( lineStart )
		posTwo = data.find( "\n", posOne )
		substring = data[posOne:posTwo]
		return substring
	else:
		job.log("\tCould not find \"" + lineStart + "\"" )
		return None

class OutputSelectHandler(ActionListener):
	def __init__(self, job, callback):
		self.job = job
		self.callback = callback

	def actionPerformed(self, event):
		self.job.askForOutputDir()
		if self.callback:
			self.callback()

class CancelHandler(ActionListener):
	def __init__(self, job, frame):
		self.job = job
		self.frame = frame

	def actionPerformed(self, event):
#		global currently_asking
#		self.job.log("Canceling stitching")
#		currently_asking = False
		self.frame.setVisible(False)
		self.frame.dispose()

class StartHandler(ActionListener):

	def __init__(self, job, frame, yamlonly):
		self.job = job
		self.frame = frame
		self.yamlonly = yamlonly

	def actionPerformed(self, event):
#		global currently_asking
		self.job.projectName = projectNameTf.getText()
		scaleLevels = scaleLevelsTf.getText()
		if scaleLevels == "":
			self.job.setScaleLevels(0)
		else:
			try:
				self.job.setScaleLevels(int(scaleLevelsTf.getText()))
			except:
				print("ERR: invalid value for Scale Levels")

		resolution = [float(r.strip()) for r in resolutionTf.getText().split(",")]
		if len(resolution) != 3:
			print("ERR: invalid resolution")
			return
		else:
			job.resolution = resolution

		self.job.metadata = [tf.getText() for tf in formMetadata]
		self.job.colors = [tf.getSelectedItem() for tf in formColors]

		#self.job.
		self.job.outputPathWithoutExtension = os.path.join( job.outputDir, job.projectName )
		self.job.ready = True
		print("Starting job")
		# Save the job's properties
#		self.job.saveProperties()

		self.job.saveYaml( )

		if not self.yamlonly:
			self.job.saveHDF5()


		# Stop input
#		currently_asking = False
		self.frame.setVisible(False)
		self.frame.dispose()

# Get Image
def getImage():
	"""Get first open image or open Bio-Formats importer if no image is
	available."""
	imp = WindowManager.getCurrentImage()
	if not imp:
		# Show bio formats open dialog
		IJ.run("Bio-Formats")
		imp = WindowManager.getCurrentImage()
		print("Start")
	if not imp:
		IJ.noImage()
		raise ValueError("Need image")	
	return imp

imp = getImage()
print( "Using open image: " + str(imp))

job = ConvertJob(imp)

fileInfo = imp.getOriginalFileInfo()

if not fileInfo:
	# Ask for ourput dir
	job.askForOutputDir()
else:
	job.outputDir = fileInfo.directory

job.validate()

#imgInfo = Info()
#p = imp.getChannelProcessor()
#info = imgInfo.getImageInfo( imp, imp.getChannelProcessor() )
#job.metadata = info


# Construct GUI
# -------------

infoPanel = JPanel()
infoPanel.setLayout(GridLayout(1,1))
infoPanel.add(JLabel("<html>Convert the currently opened image to HDF5+YAML<br>The resulting files will be placed in the output directory specified.<br>Existing HDF5 and YAML files <i>will be overwritten</i>.</html>"))

frame = JFrame("Convert to HDF5+YAML")
inputPanel = JPanel()
#inputPanel.setLayout(GridLayout(4 + 4*job.channels, 2))
inputPanel.setLayout(GridLayout(0, 2))

inputPanel.add(JLabel("Image Dimensions"))
inputPanel.add(JLabel( str(job.width) + " x " + str(job.height) + ", " + str(job.slices) + " slices, " + str(job.channels) + " stacks" ))

projectNameTf = JTextField(job.projectName)
inputPanel.add(JLabel("Image Name"))
inputPanel.add(projectNameTf)

scaleLevelsTf = JTextField()
inputPanel.add(JLabel("Scale levels (empty=automatic)"))
inputPanel.add(scaleLevelsTf)

resolutionTf = JTextField()
inputPanel.add(JLabel("Resolution"))
inputPanel.add(resolutionTf)
resolutionTf.setText("%s, %s, %s" % job.resolution)
inputPanel.add(JLabel("Output Directory"))
outputDirPanel = JPanel()
outputDirPanel.setLayout(BorderLayout())
outputDirTf = JTextField(job.outputDir)
outputDirPanel.add(outputDirTf, BorderLayout.CENTER)
outputDirSelect = JButton("...")

def refreshOutput():
	if job.outputDir:
		outputDirTf.setText(job.outputDir)
	else:
		outputDirTf.setText("")

outputDirSelect.addActionListener(OutputSelectHandler(job, refreshOutput))
outputDirPanel.add(outputDirSelect, BorderLayout.EAST)
inputPanel.add(outputDirPanel)

# Generate default metadata string
# mdList = []
# offsetStr = "Image " + str(i) + " : AnalogPMTOffset = "
# offsetData = getDataPart( job, job.metadata, offsetStr )
# if offsetData != None:
# 	mdList.append( "PMT Offset: " + offsetData )
# powerStr = "Image " + str(i) + " : ExcitationOutPutLevel = "
# powerData =  getDataPart( job, job.metadata, powerStr )
# if powerData != None:
# 	mdList.append( "Laser Power: " + powerData )
# gainStr = "Image " + str(i) + " : PMTVoltage = "
# gainData =  getDataPart( job, job.metadata, gainStr )
# if gainData != None:
# 	mdList.append( "PMT Voltage: " + gainData )
# mdString = ", ".join( mdList )

# Channels / Stacks
formChannelNames = []
formMetadata = []
formColors = []

for i in range( 0, job.channels ):
	inputPanel.add(JLabel("Stack " + str(i+1)))
	inputPanel.add(JLabel(""))

	inputPanel.add(JLabel("  Name"))
	formChannelNames.append(JTextField("Channel " + str(i+1) ))
	inputPanel.add(formChannelNames[i])

	inputPanel.add(JLabel("  Metadata"))
	formMetadata.append(JTextField(""))
	inputPanel.add(formMetadata[i])

	inputPanel.add(JLabel("  Color"))
	formColors.append(Choice())
	formColors[i].add("Red")
	formColors[i].add("Green")
	formColors[i].add("Blue")
	formColors[i].add("Cyan")
	formColors[i].add("Magenta")
	formColors[i].add("Yellow")
	formColors[i].add("White")
	formColors[i].select(i)
	inputPanel.add(formColors[i])


controlPanel = JPanel()
controlPanel.setLayout(GridLayout(1,3))
stopBtn = JButton("Cancel")
stopBtn.addActionListener(CancelHandler(job, frame))
controlPanel.add(stopBtn)
yamlBtn = JButton("Generate YAML only")
yamlBtn.addActionListener(StartHandler(job, frame, True))
controlPanel.add(yamlBtn)
startBtn = JButton("Generate HDF5+YAML")
startBtn.addActionListener(StartHandler(job, frame, False))
controlPanel.add(startBtn)

frame.getContentPane().add(infoPanel, BorderLayout.NORTH)
frame.getContentPane().add(JScrollPane(inputPanel), BorderLayout.CENTER)
frame.getContentPane().add(controlPanel, BorderLayout.SOUTH)
frame.pack()
frame.setVisible(True)