# -*- coding: utf-8 -*-
#!/usr/bin/python

#Needs at least 2G of RAM
#Also needs space on the temporary folder. If you run out of space, maybe you should
#remount the /tmp folder with "sudo mount -o remount,size=2000M /tmp"

import sys
import codecs
import string
import getopt
import shapefile
from dbfpy import dbf
import random
import tempfile
import progressbar
import threading
from multiprocessing import Process
from functools import wraps

numlines = 0


def run_async(func):
  @wraps(func)
  def async_func(*args, **kwargs):
          func_hl = Process(target = func, args = args, kwargs = kwargs)
          func_hl.start()
          return func_hl

  return async_func



def openShapefile(url):
 return shapefile.Reader(url, 'r')

def writeNode(file, attrs={}):
  if not attrs.has_key("user"):
    attrs["user"] = "navteq2osm"
  if not attrs.has_key("uid"):
    attrs["uid"] = "1"
  if not attrs.has_key("timestamp"):
    attrs["timestamp"] = "2012-01-19T19:07:25Z"
  if not attrs.has_key("visible"):
    attrs["visible"] = "true"
  if not attrs.has_key("version"):
    attrs["version"] = "1"
  if not attrs.has_key("changeset"):
    attrs["changeset"] = "1" 
    
  writeToFile(file,"  <node")
  for k in attrs:
    writeToFile(file," " + str(k) + str('="') + str(attrs.get(k)) + str('"'))
  writeToFile(file," />\n")
    
def writeToFile(file, line):
    file.write(line.decode("utf-8"))
    global numlines
    numlines = numlines + 1
    if random.random() > 0.75 :
      file.flush()
    
def getNode(point, id_nodes):
  x = point[0]
  y = point[1]
  n_attrs = {}

  n_attrs['id'] = id_nodes
  n_attrs['lon'] = x
  n_attrs['lat'] = y
  
  return n_attrs

def writeWay(file, attrs, nodes, tags):
  #Write way to file
  writeToFile(file,"  <way")
  
  if not attrs.has_key("user"):
    attrs["user"] = "navteq2osm"
  if not attrs.has_key("uid"):
    attrs["uid"] = "1"
  if not attrs.has_key("timestamp"):
    attrs["timestamp"] = "2012-01-19T19:07:25Z"
  if not attrs.has_key("visible"):
    attrs["visible"] = "true"
  if not attrs.has_key("version"):
    attrs["version"] = "1"
  if not attrs.has_key("changeset"):
    attrs["changeset"] = "1" 
  
  for k in attrs:
    writeToFile(file," " + str(k) + "=" + str('"') + str(attrs.get(k)) + str('"') )
  writeToFile(file," >\n")
  for n in nodes:
    writeToFile(file,"     <nd ref=" + str('"') + str(n) + str('"') + " />\n")
  for k in tags:
    try:
      writeToFile(file,'     <tag k="' + str(k) + '" v="' + str(tags.get(k)) + '" />\n')
    except:
      print("Exception: " + str(k) + " = " + str(tags.get(k)))
  writeToFile(file,"  </way>\n")
  
def printHelp():
  print("Usage NavTeq2OSM.py --i path-to-shapefiles --o output-file")
  exit(1)
  
def printRelation(file, rid, source, target, via={}, tags={}, attrs={}):
  
  if not attrs.has_key("user"):
    attrs["user"] = "navteq2osm"
  if not attrs.has_key("uid"):
    attrs["uid"] = "1"
  if not attrs.has_key("timestamp"):
    attrs["timestamp"] = "2012-02-19T19:07:25Z"
  if not attrs.has_key("version"):
    attrs["version"] = "1"
    
  attrs["id"] = rid
  if not attrs.has_key("changeset"):
    attrs["changeset"] = "1" 
    
  writeToFile(file,'  <relation ')
  for k in attrs:
    writeToFile(file," " + str(k) + "=" + str('"') + str(attrs.get(k)) + str('"') )
  writeToFile(file," >\n")
    
  writeToFile(file,'    <member type="way" ref="' + str(source) + '" role="from" />\n')
  writeToFile(file,'    <member type="way" ref="' + str(target) + '" role="to" />\n')
  for k in via:
    writeToFile(file,'    <member type="' + str(via[k]) + '" ref="' + str(k) + '" role="via" />\n')
  
  for k in tags:
    writeToFile(file,'     <tag k="' + str(k) + '" v="' + str(tags.get(k)) + '" />\n')
    
  writeToFile(file,"  </relation>\n")  
  
def processStreets(zlevels, rdms, cdms, shpfile, nodes_file, ways_file, progress, relations_file):
  
  ways = {}
  nodes = {}
  
  node_cont = 1
  node_id = -1
  
  last_node_id = None
  last_level = None
  last_link_id = None
  
  #walking through all nodes
  for i in range(0, zlevels.numRecords):
    progress.update(i)
    record = zlevels.shapeRecord(i)
    
    link_id = record.record[0]
    level = record.record[3]
    
    #Nodes array saves nodes_id by coordinates and level
    point = record.shape.points[0]
    node_key = str(point[0]) + "_" + str(point[1]) + "_" + str(level)
      
    #Calculate node_id
    if not nodes.has_key(node_key): #We haven't already saved it
      if record.record[4] == 'Y': #Only saving intersections
        nodes[node_key] = node_cont
      node_id = node_cont
      node_cont = node_cont + 1
      writeNode(nodes_file, getNode(record.shape.points[0], node_id))
    else: #We saved this node before
      if record.record[4] == 'Y': #Only interested on intersections
        node_id = nodes[node_key]
      else:
        node_id = node_cont
        node_cont = node_cont + 1
        writeNode(nodes_file, getNode(record.shape.points[0], node_id))
    
    #Save reference of node to ways array
    if not ways.has_key(link_id) :
      ways[link_id] = []
    ways[link_id].append(node_id)
      
    #write node definition to file
    
  
  t2 = processRDMS(rdms, cleanCDMS(cdms, [3, 7, 8, 21]), relations_file, ways)
  
  
  cdms_ = cleanCDMS(cdms, [2])
  cdms = None
  rdms = None
  nodes = None
      
  cont = zlevels.numRecords
  zlevels = None
  record = None
  node_cont = None
  node_id = None
  
  #Walking through all ways (vials)
  for i in range(0, len(shpfile)):

    progress.update(cont + i)

    shpRecord = shpfile[i]
    tags={}
    attrs={}
    
    attrs['id'] = shpRecord[0]
    
    fclass = shpRecord[23]
    if fclass == '1':
      tags['highway'] = 'primary'
    elif fclass ==  '2':
      tags['highway'] = 'trunk'
    elif fclass ==  '3':
      tags['highway'] = 'secondary'
    elif fclass ==  '4':
      tags['highway'] = 'tertiary'
    elif fclass ==  '5':
      tags['highway'] = 'residential'
      
      
    name = shpRecord[1]
    name = name.strip()
    
    #Some strange characters osm does not like:
    name = string.replace(name, str('"'), str("'")) 
    name = string.replace(name, str('&'), str(" ")) 
    
    #Just to be sure about codification, even when it looks stupid
    name = name.decode("utf-8").encode("UTF-8")
    if len(name) > 0:
      tags['name'] = name
    
    dir_travel = shpRecord[32]
    if dir_travel == 'F':
      tags['oneway'] = 'yes'
    elif dir_travel == 'T':
      tags['oneway'] = '-1'
    else:
      tags['oneway'] = 'no'
    
    if ways.has_key(shpRecord[0]):
      nodes = ways[shpRecord[0]]
   #   ways[shpRecord[0]] = None
    else:
      print("Error: way without nodes!!  " + str(shpRecord[0]))
    
    #Commented because it takes too long
    tags = getWayTags(cdms_, attrs['id'], tags)
    writeWay(ways_file, attrs, nodes, tags)
    
  t2.join()
    
    
#Instead of having to search through all the data, we just save in memory
#the data we will be using.
def cleanCDMS(cdms, cond=[]):
  cdms_ = []
  
  try:
    for cdm in cdms:
      for c in cond:
        if c == cdm[2]:
          cdms_.append(cdm)
  except:
    pass
  
  return cdms_
  
@run_async
def processRDMS(rdms, cdms, file, ways = {}):
  
  tags = {}
  via = {}
  
  for rdm in rdms:
    if rdm[3] == 1:
      if tags.has_key('type'):
        if len(via) == 0:
          via = calculateVia(way_from, way_to, ways)
        #We need to write afterwards because of the via members
        printRelation(file, restriction_id,  way_from, way_to, via, tags)
      
      via = {}
      restriction_id = rdm[1]
      tags = getRestrictionTags(cdms, restriction_id, {})
      way_from = rdm[0]
      way_to = rdm[2]
      
    else:
      via[way_to] = "way"
      way_to = rdm[2]
      
  #We need to write afterwards because of the via members
  if tags.has_key('type'):
    if len(via) == 0:
      via = calculateVia(way_from, way_to, ways)
    printRelation(file, restriction_id, way_from, way_to, via, tags)
    
    
def getRestrictionTags(cdms, res_id, tags={}):
  try:
    for cdm in cdms:
      if cdm[1] == res_id:
        if cdm[2] == 3: #Construction Status Closed
          tags['type'] = 'restriction'
          tags['restriction'] = 'no_entry'
        elif cdm[2] == 7: #Restricted Driving Manoeuvre
          tags['type'] = 'restriction'
          tags['restriction'] = 'no_straight_on'          
        elif cdm[2] == 8: #Access Restriction
          tags['type'] = 'restriction'
          tags['restriction'] = 'no_straight_on'
        #elif cdm[2] == 9: #Special Explication
          #tags['type'] = None
        #elif cdm[2] == 12: #Usage Fee Required
          #tags['type'] = None
        #elif cdm[2] == 13: #Lane Traversal
          #tags['type'] = None
        #elif cdm[2] == 14: #Through Route
          #tags['type'] = None
        #elif cdm[2] == 18: #Railway Crossing
          #tags['type'] = None
        #elif cdm[2] == 19: #Passing Restriction
          #tags['type'] = None
        #elif cdm[2] == 20: #Junction View
          #tags['type'] = None
        elif cdm[2] == 21: #Protected Overtaking
          tags['type'] = 'overtaking'
          
        #exceptions
        tags['except'] = ''
        if cdm[8] == 'N':
          tags['except'] = tags['except'] + 'motorcar;'
        if cdm[9] == 'N' or cdm[10] == 'N' or cdm[15] == 'N':
          tags['except'] = tags['except'] + 'psv;'
        if cdm[12] == 'N':
          tags['except'] = tags['except'] + 'bicycle;'
        if cdm[13] == 'N':
          tags['except'] = tags['except'] + 'hgv;'
          
        if tags['except'] == '':
          del tags['except']
        break
  except:
    pass
  
  return tags
  
def getWayTags(cdms, res_id, tags={}):
  
  try:
    for cdm in cdms:
      if cdm[0] == res_id:
        if cdm[2] == 2: #Toll structure
          tags['toll'] = 'yes'
        #elif cdm[2] == 4: #Gate
          #tags['type'] = None
        #elif cdm[2] == 5: #Direction of Travel
          #tags['type'] = None
        #elif cdm[2] == 7: #Restricted Driving Manoeuvre
          #tags['type'] = None
        #elif cdm[2] == 9: #Special Explication
          #tags['type'] = None
      # elif cdm[2] == 10: #Special Speed Situation
      #   tags['type'] = 'restriction'
      #   condition modifier 1: 1-advisory, 2-dependent, 3-speed bumps present  
      # elif cdm[2] == 11: #Variable Sign Speed
      #   tags['type'] = None
      # elif cdm[2] == 12: #Usage Fee Required
      #   tags['type'] = None
      # elif cdm[2] == 13: #Lane Traversal
      #   tags['type'] = None
      # elif cdm[2] == 14: #Through Route
      #   tags['type'] = None
        #elif cdm[2] == 19: #Passing Restriction
          #tags['type'] = None
        #elif cdm[2] == 20: #Junction View
          #tags['type'] = None
        break
  except ValueError:
    pass
  return tags
   
    
def calculateVia(source, target, ways = {}):
  via = {}
  
  if ways.has_key(source) and ways.has_key(target):
    source_a = ways[source][0]
    target_a = ways[target][0]
    source_b = ways[source][len(ways[source]) - 1]
    target_b = ways[target][len(ways[target]) - 1]
  
    if source_a == source_b:
      via[source_a] = "node"
    elif source_a == target_a:
      via[source_a] = "node"
    elif source_a == target_b:
      via[source_a] = "node"
    else:
      via[source_b] = "node"
  
  return via
  
def main(argv):
  output_file = None
  shp_path = None
  for opt,arg in argv:
    if opt in ("--input-path"):
      shp_path = arg
    elif opt in ("-o", "--output-file"):
      output_file = codecs.open(arg, "w", "utf_8_sig")
    elif opt in ("-h", "--help"):
      printHelp()
      
  if output_file is None or shp_path is None:
    printHelp()
  
  
  shpfile = dbf.Dbf(shp_path + "/Streets.dbf")
  rdms = dbf.Dbf(shp_path + "/Rdms.dbf")
  zlevels = openShapefile(shp_path + "/Zlevels")
  cdms = dbf.Dbf(shp_path + "/Cdms.dbf")
  
  
  nodes_file_ = tempfile.NamedTemporaryFile()
  nodes_file = codecs.open(nodes_file_.name, "r+w", "utf-8")
  ways_file_ = tempfile.NamedTemporaryFile()
  ways_file = codecs.open(ways_file_.name, "r+w", "utf-8")
  relations_file_ = tempfile.NamedTemporaryFile()
  relations_file = codecs.open(relations_file_.name, "r+w", "utf-8")
  
  widgets = ['Importing data from NavTeq Shapes: ', progressbar.Bar(marker=progressbar.AnimatedMarker()),
           ' ', progressbar.ETA()]
  maxval = len(shpfile) + zlevels.numRecords
  progress = progressbar.ProgressBar(widgets=widgets, maxval=maxval).start()
  progress.update(0)
  
  processStreets(zlevels, rdms, cdms, shpfile, nodes_file, ways_file, progress, relations_file)
  
  if not progress is None:
    progress.finish()

  #Free memory:
  shpfile = None
  progress=None
  rdms = None
  n = None
  via = None
  record = None
  restriction_id = None
  restriction_type = None
  way_from = None
  way_to = None
  
    
  global numlines
  progress = progressbar.ProgressBar(widgets=['Writing data to file .osm, please wait: ', progressbar.Percentage(), progressbar.Bar(marker=progressbar.AnimatedMarker()),
           ' ', progressbar.ETA()], maxval=numlines + 10).start()
           
  progress.update(0)
  output_file.write("<?xml version='1.0' encoding='UTF-8'?>")
  output_file.write('\n')
  output_file.write(" <osm version='0.6' generator='navteq2osm'>")
  output_file.write('\n')
  output_file.flush()
  
  nodes_file.seek(0)
  for line in nodes_file:
    output_file.write(line)
    progress.update(progress.currval + 1)
    if random.random() > 0.7 :
      output_file.flush()
  nodes_file.close()
  nodes_file_.close()
    
  ways_file.seek(0)
  for line in ways_file:
    output_file.write(line)
    progress.update(progress.currval + 1)
    if random.random() > 0.7 :
      output_file.flush()
  ways_file.close()
  ways_file_.close()
    
  relations_file.seek(0)
  for line in relations_file:
    output_file.write(line)
    progress.update(progress.currval + 1)
    if random.random() > 0.7 :
      output_file.flush()
  relations_file.close()
  relations_file_.close()
      
  output_file.write(' </osm>')
  output_file.write('\n')
  output_file.flush()
  output_file.close()
  progress.finish()
      
if __name__ == "__main__":
  options, remainder = getopt.getopt(sys.argv[1:], 'o:v', ['input-path=', 'help', 'output-file='])
  main(options)
