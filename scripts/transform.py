import sys
import yaml
import os


filename = sys.argv[1]

stream = file(filename, 'r')
data = yaml.load(stream)

# Name extrahieren
name = data['project']['name']

# neuen Namen eintragen
data['project']['title'] = "DOT"
del data['project']['name']

# Stackgroups eintragen
if not 'stackgroups' in data['project']:
	data['project']['stackgroups'] = [{'title': name, 'min': 0, 'max':255}]

# Dateiname als "path"
for stack in data['project']['stacks']:
	folder = stack['folder']
	del stack['folder']

	stack['stackgroups'] = [{
			'name': name,
			'relation': 'has_channel'}]
	stack['mirrors'] = [{
			'title': 'Dresden',
			'folder':folder,
			'tile_source_type':'9',
			'path':os.path.splitext(filename)[0]}]



print yaml.dump( data, default_flow_style=False)

