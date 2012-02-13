# -*- coding: utf-8 -*-
#!/usr/bin/python

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
from threading import Thread
from functools import wraps


def run_async(func):
  @wraps(func)
  def async_func(*args, **kwargs):
          func_hl = Thread(target = func, args = args, kwargs = kwargs)
          func_hl.start()
          return func_hl

  return async_func



def openShapefile(url):
 # myshp = codecs.open(str(url)+".shp", "r")#, "utf-16")
 # mydbf = codecs.open(str(url)+".dbf", "r")#, "utf-16")
 # return shapefile.Reader(shp=myshp, dbf=mydbf)
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
#  try:
    file.write(line.decode("utf-8"))
    if random.random() > 0.75 :
      file.flush()
#  except:
#    print("Error writing " + str(line) + " to file ")
    
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
      writeToFile(file,"     <tag " + str(k) + "=" + str('"')  + str(tags.get(k)) + str('" />\n'))
    except:
      print("Exception: " + str(k) + " = " + str(tags.get(k)))
  writeToFile(file,"  </way>\n")
  
def printHelp():
  print("Usage NavTeq2OSM.py --i path-to-shapefiles --o output-file")
  exit(1)
  
def printRelation(file, id, type, source, target, via={}):
  writeToFile(file,'  <relation id="' + str(id) + '" type="restriction" restriction="' + str(type))
  writeToFile(file, '" version="1" changeset="1" user="navteq2osm" uid="1" visible="true" timestamp="2012-01-19T11:40:26Z">\n')
  writeToFile(file,'    <member type="way" ref="' + str(source) + '" role="from" />\n')
  writeToFile(file,'    <member type="way" ref="' + str(target) + '" role="to" />\n')
  for k in via:
    writeToFile(file,'    <member type="' + str(via[k]) + '" ref="' + str(k) + '" role="via" />\n')
  writeToFile(file,"  </relation>\n")  
   
@run_async
def processStreets(zlevels, shpfile, nodes_file, ways_file, progress, relations_file):
  
  ways = {}
  nodes = {}
  
  restriction_id = 1
  node_cont = 0
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
    name = string.replace(name, str('"'), str("'")) #TODO
    name = string.replace(name, str('&'), str(" ")) #TODO
    name = name.decode("utf-8").encode("UTF-8")
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
      ways[shpRecord[0]] = None
    else:
      print("Error: way without nodes!!  " + str(shpRecord[0]))
    
    writeWay(ways_file, attrs, nodes, tags)
    
  if not progress is None:
    progress.finish()
    
  
@run_async
def processRDMS(rdms, file, progress):
  
  restriction_type = None
  for i in range(0, len(rdms)):
    record = rdms[i]
    
    if record[3] == 1:
      if restriction_type is not None:
        printRelation(file, restriction_id, restriction_type, way_from, way_to, via)
        
      #Generating new restriction
      via = {}
      #TODO:
      restriction_type = "no_right_turn"
      way_from = record[0]
      way_to = record[2]
      restriction_id = record[1]
      
    else:
      via[record[2]] = "way"
      way_to = record[2]
      restriction_id = record[1]
      
  if restriction_type is not None:
    printRelation(file, restriction_id, restriction_type, way_from, way_to, via)
    
   
  
def main(argv):
  output_file = None
  shp_path = None
  for opt,arg in argv:
    if opt in ("--input-path"):
      shp_path = arg
    elif opt in ("-o", "--output-file"):
      output_file = codecs.open(arg, "r+w", "utf_8_sig")
    elif opt in ("-h", "--help"):
      printHelp()
      
  if output_file is None or shp_path is None:
    printHelp()
  
  
  shpfile = dbf.Dbf(shp_path + "/Streets.dbf")
  rdms = dbf.Dbf(shp_path + "/Rdms.dbf")
  zlevels = openShapefile(shp_path + "/Zlevels")
  
  
  nodes_file_ = tempfile.NamedTemporaryFile()
  nodes_file = codecs.open(nodes_file_.name, "r+w", "utf-8")
  ways_file_ = tempfile.NamedTemporaryFile()
  ways_file = codecs.open(ways_file_.name, "r+w", "utf-8")
  relations_file_ = tempfile.NamedTemporaryFile()
  relations_file = codecs.open(relations_file_.name, "r+w", "utf-8")
  relations_file2_ = tempfile.NamedTemporaryFile()
  relations_file2 = codecs.open(relations_file_.name, "r+w", "utf-8")
  
  widgets = ['Importing data from NavTeq Shapes: ', progressbar.Bar(marker=progressbar.AnimatedMarker()),
           ' ', progressbar.ETA()]
  maxval = len(shpfile) + zlevels.numRecords
  progress = progressbar.ProgressBar(widgets=widgets, maxval=maxval).start()
  progress.update(0)
  
  t1 = processStreets(zlevels, shpfile, nodes_file, ways_file, progress, relations_file2)
  t2 = processRDMS(rdms, relations_file, progress)
  
  t1.join()
  t2.join()

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
  
    
  print("Writing data to file .osm")
  
  
  writeToFile(output_file,"<?xml version='1.0' encoding='UTF-8'?>")
  writeToFile(output_file,'\n')
  writeToFile(output_file," <osm version='0.6' generator='navteq2osm'>")
  writeToFile(output_file,'\n')
  
  nodes_file.seek(0)
  for line in nodes_file:
    output_file.write(line)
    if random.random() > 0.7 :
      output_file.flush()
  nodes_file.close()
  nodes_file_.close()
    
  ways_file.seek(0)
  for line in ways_file:
    output_file.write(line)
    if random.random() > 0.7 :
      output_file.flush()
  ways_file.close()
  ways_file_.close()
    
  relations_file.seek(0)
  for line in relations_file:
    output_file.write(line)
    if random.random() > 0.7 :
      output_file.flush()
  relations_file.close()
  relations_file_.close()
    
  relations_file2.seek(0)
  for line in relations_file2:
    output_file.write(line)
    if random.random() > 0.7 :
      output_file.flush()
  writeToFile(output_file,' </osm>')
  output_file.flush()
  relations_file2.close()
  relations_file2_.close()
  
  output_file.close()
      
if __name__ == "__main__":
  options, remainder = getopt.getopt(sys.argv[1:], 'o:v', ['input-path=', 'help', 'output-file='])
  main(options)
