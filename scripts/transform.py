#!/usr/bin/python3
# -*- coding: utf-8 -*-
import sys
import yaml
import os


filename = sys.argv[1]

stream = open(filename, 'r')
data = yaml.load(stream)

# Name extrahieren
name = data['project']['name']

# Get old maximum value
max_val = data['project'].get('valuelimit')
min_val = 0
if max_val:
    del data['project']['valuelimit']

# neuen Namen eintragen
data['project']['title'] = "DOT"
del data['project']['name']

# Stackgroups eintragen
if not 'stackgroups' in data['project']:
        sg_data = {
            'title': name
        }
        if max_val:
            sg_data['min'] = min_val
            sg_data['max'] = max_val

        data['project']['stackgroups'] = [sg_data]

# Dateiname als "path"
for stack in data['project']['stacks']:
        folder = stack['folder']
        del stack['folder']

        stack_max_val = stack.get('valuelimit')
        stack_min_val = 0

        stack['title'] = u'{} - {}'.format(name, stack['name'])
        del stack['name']

        if stack_max_val:
            del stack['valuelimit']
            stack['min'] = stack_min_val
            stack['max'] = stack_max_val

        metadata = stack.get('metadata')
        if metadata:
            stack['description'] = metadata.strip("'")
            del stack['metadata']

        stack['stackgroups'] = [{
                        'title': name,
                        'relation': 'has_channel'}]
        stack['mirrors'] = [{
                        'title': 'Dresden',
                        'folder': folder,
                        'tile_source_type': 9,
                        'path': os.path.splitext(filename)[0]}]



print(yaml.safe_dump( data, default_flow_style=False))

