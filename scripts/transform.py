import sys
import yaml
import os


filename = sys.argv[1]

stream = file(filename, 'r')
data = yaml.load(stream)

# Name extrahieren
name = data['project']['name']

# neuen Namen eintragen
data['project']['name'] = "DOT"

# Stackgroups eintragen
if not 'stackgroups' in data['project']:
	data['project']['stackgroups'] = [{'name': name, 'min': 0, 'max':255}]

# Dateiname als "path"
for stack in data['project']['stacks']:
	stack['path'] = os.path.splitext(filename)[0]
	stack['stackgroups'] = [{'name': name, 'relation': 'has_channel'}]



print yaml.dump( data, default_flow_style=False)

