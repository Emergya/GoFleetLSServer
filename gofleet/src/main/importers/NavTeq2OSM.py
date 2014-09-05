# -*- coding: utf-8 -*-
#!/usr/bin/python

#Needs at least 2G of RAM
#Also needs space on the temporary folder. If you run out of space, maybe you should
#remount the /tmp folder with "sudo mount -o remount,size=2000M /tmp"

import sys, traceback
import codecs
import string
import getopt
import shapefile
import dbf
import random
import tempfile
import progressbar
import threading
from multiprocessing import Process
from functools import wraps
from numpy import array, dot, sqrt, arccos, pi, arctan2

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

  if nodes is None:
#    if attrs.has_key('id'):
#      print "Way without nodes: " + str(attrs['id'])
    return

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
      traceback.print_exc()
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

def process(zlevels, rdms, cdms, shpfile, nodes_file, ways_file, progress, relations_file):

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

  link_id = None
  node_key = None
  record = None
  node_cont = None
  node_id = None
  cont = zlevels.numRecords
  zlevels = None
  last_node_id = None
  last_level = None
  last_link_id = None

  t2 = processRDMS(rdms, cleanCDMS(cdms, [3, 7, 8, 21]), relations_file[0], ways)
  rdms = rdms.close()
  rdms = None
  t7 = processVials(shpfile, progress, ways, ways_file, nodes, cleanCDMS(cdms, [2]), cont)
  cdms.close()
  cdms = None

  max = float(len(shpfile))

  t3 = processRDMS_divider(relations_file[1], ways, shpfile.name, nodes_file.name, progress, 0, round(max/4), 1)
  t4 = processRDMS_divider(relations_file[2], ways, shpfile.name, nodes_file.name, progress, round(max/4) + 1, 2 * round(max/4), 2)
  t5 = processRDMS_divider(relations_file[3], ways, shpfile.name, nodes_file.name, progress, 2 * round(max/4) + 1, 3 * round(max/4), 3)
  t6 = processRDMS_divider(relations_file[4], ways, shpfile.name, nodes_file.name, progress, 3 * round(max/4) + 1, max, 4)

  shpfile = None

  t2.join()
  t3.join()
  t4.join()
  t5.join()
  t6.join()
  t7.join()


@run_async
def processVials(shpfile, progress, ways, ways_file, nodes, cdms, cont):

  #Walking through all ways (vials)
  for i in range(0, len(shpfile)):
    try:
      progress.update(cont + i)
    except:
      pass
    try:
      shpRecord = shpfile[i]
    except:
      print "Malformed DBF Street[" + str(i) + "]"
      continue

    try:
      way_id = shpRecord[0]
    except:
      print "Malformed DBF Street[" + str(i) + "]:: way_id"
      continue

    try:
      name = shpRecord[1]
    except:
      print "Malformed DBF Street[" + str(i) + "]::name"
      continue

    try:
      fclass = shpRecord[23]
    except:
      print "Malformed DBF Street[" + str(i) + "]::fclass"
      continue

    try:
      dir_travel = shpRecord[32]
    except:
      print "Malformed DBF Street[" + str(i) + "]::dir_travel"
      continue

    try:
      tags={}
      attrs={}

      attrs['id'] = way_id

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
    except:
      print "Error extracting highway (" + str(fclass) + "):", sys.exc_info()[0]
      traceback.print_exc()

    try:
      name = name.strip()

      #Some strange characters osm does not like:
      name = string.replace(name, str('"'), str("'"))
      name = string.replace(name, str('&'), str(" "))

      #Just to be sure about codification, even when it looks stupid
      name = name.decode("utf-8").encode("UTF-8")
      if len(name) > 0:
        tags['name'] = name
    except:
      print "Error extracting name (" + str(name) + "):", sys.exc_info()[0]
      traceback.print_exc()

    try:
      if dir_travel == 'F':
        tags['oneway'] = 'yes'
      elif dir_travel == 'T':
        tags['oneway'] = '-1'
      else:
        tags['oneway'] = 'no'
    except:
      print "Error extracting oneway (" + str(dir_travel) + "):", sys.exc_info()[0]
      traceback.print_exc()

    try:

      if ways.has_key(way_id):
        nodes = ways[way_id]
        ways[way_id] = None
     # else:
     #   print("Error: way (" + str(way_id) + ") without nodes!! i=" + str(i))

      #Commented because it takes too long
      tags = getWayTags(cdms, way_id, tags)
      writeWay(ways_file, attrs, nodes, tags)
    except:
      print "Error processing streets (" + str(i) + "):", sys.exc_info()[0]
      traceback.print_exc()


  shpfile = None
  ways = None
  cdms = None

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
    print "Error processing cdms", sys.exc_info()[0]
    traceback.print_exc()

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


@run_async
def processRDMS_divider(relations_file_, ways, streetsname, nodes_file_name, progress, min=0, max=1000, res_id=1):

  res_id = res_id * 1000000

  nodes_file = open(nodes_file_name, "r")
  streets = dbf.Dbf(streetsname, "r")
  while nodes_file: #Walking through all ways (vials)
    for i in range(int(min), int(max)):
      try:
        progress.update(cont + i)
      except:
        pass
      try:
        street_ = streets[i]
      except:
        pass
      try:
        #print "id " +  str(street_[0])
        divider = street_[31]
        if divider == 'A' or divider == '1' or divider == '2':
          #print "has divider"
          way_id = street_[0]
          if not ways.has_key(way_id):
            continue
          street = ways[way_id]
          node_a = street[0]
          node_b = street[len(ways[way_id]) - 1]
          for k in ways:
            if k == way_id:
              continue
            try:
              way = ways[k]

              #We search for the angle of the intersections
              #If it is a left turn, it is forbidden
              if way[0] == node_a \
                          and angle(searchLatLon(nodes_file, street[1]), \
                                    searchLatLon(nodes_file, node_a), \
                                    searchLatLon(nodes_file, way[1])) < 0:
                printRelation(relations_file_, res_id, k, way_id, {node_a: 'node'}, {'type': 'restriction', 'type': 'no_right_turn'})
                res_id = res_id + 1
              elif way[len(way) - 1] == node_a \
                          and angle(searchLatLon(nodes_file, street[1]), \
                                    searchLatLon(nodes_file, node_b), \
                                    searchLatLon(nodes_file, way[len(way) - 2])) < 0:
                printRelation(relations_file_, res_id, k, way_id, {node_b: 'node'}, {'type': 'restriction', 'type': 'no_right_turn'})
                res_id = res_id + 1
              elif way[0] == node_b \
                          and angle(searchLatLon(nodes_file, street[len(street) - 2]), \
                                    searchLatLon(nodes_file, node_a), \
                                    searchLatLon(nodes_file, way[1])) < 0:
                printRelation(relations_file_, res_id, k, way_id, {node_a: 'node'}, {'type': 'restriction', 'type': 'no_right_turn'})
                res_id = res_id + 1
              elif way[len(way) - 1] == node_b \
                          and angle(searchLatLon(nodes_file, street[len(street) - 2]), \
                                    searchLatLon(nodes_file, node_b), \
                                    searchLatLon(nodes_file, way[len(way) - 2])) < 0:
                printRelation(relations_file_, res_id, k, way_id, {node_b: 'node'}, {'type': 'restriction', 'type': 'no_right_turn'})
                res_id = res_id + 1
            except AttributeError as ae:
              print "AttributeError generating relation: i=" + str(i) + ", way_id=" + str(way_id) + ", k=" + str(k)
              traceback.print_exc()
            except:
              print "Error generating relation:", sys.exc_info()[0]
              traceback.print_exc()
      except TypeError as te:
        traceback.print_exc()
      except:
        print "Error processing divider:"
        traceback.print_exc()
  streets.close()


def searchLatLon(nodes_file, node_id):
  nodes_file.seek(0)
  while 1:
    lines = nodes_file.readlines(100)
    if not lines:
      break
    for node in lines:
      try:
        if str(node_id) == node[(node.rfind(" id=")+5):(node.find("\"",node.find(" id=") + 5))]:
          lat = float(node[(node.rfind(" lat=") + 6):(node.find("\"",node.find(" lat=") + 6))])
          lon = float(node[(node.rfind(" lon=") + 6):(node.find("\"",node.find(" lon=") + 6))])
          return [lat,lon]
      except:
        print "Error searching Lat Lon from node:" + str(node_id), sys.exc_info()[0]
        traceback.print_exc()
  print "Node not found " + str(node_id)
  return [0, 0]

def searchNodesOnWay(ways_file, way_id):
  ways_file.seek(0)
  nodes = None
  while 1:
    lines = ways_file.readlines(400)
    if not lines:
      break
    for node in lines:
      try:
        if line.rfind("<way") > 0:
          if str(way_id) == node[(node.rfind(" id=")+5):(node.find("\"",node.find(" id=") + 5))]:
            nodes = []
          elif nodes is not None:
            return nodes

        if nodes is not None and line.rfind("<nd ref=") > 0:
          nodes.append(int(node[(line.rfind(" ref=") + 6):(line.find("\"",line.find(" ref=") + 6))]))

      except:
        pass
  return nodes

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
    print "Error processing restriction tags from :" + str(res_id), sys.exc_info()[0]
    traceback.print_exc()

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
    print "Error processing way tags from :" + str(res_id), sys.exc_info()[0]
    traceback.print_exc()
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

  relations_file = []
  relations_file.append(tempfile.NamedTemporaryFile())
  relations_file.append(tempfile.NamedTemporaryFile())
  relations_file.append(tempfile.NamedTemporaryFile())
  relations_file.append(tempfile.NamedTemporaryFile())
  relations_file.append(tempfile.NamedTemporaryFile())

  widgets = ['Importing data from NavTeq Shapes: ', progressbar.Bar(marker=progressbar.AnimatedMarker()),
           ' ', progressbar.ETA()]
  maxval = 2 * len(shpfile) + zlevels.numRecords
  progress = progressbar.ProgressBar(widgets=widgets, maxval=maxval).start()
  progress.update(0)

  process(zlevels, rdms, cdms, shpfile, nodes_file, ways_file, progress, relations_file)

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

  nodes_file.flush()
  nodes_file.seek(0)
  while 1:
    lines = nodes_file.readlines(200)
    if not lines:
      break
    else:
      for line in lines:
        output_file.write(line)
        try:
          progress.update(progress.currval + 1)
        except:
          pass
        if random.random() > 0.8 :
          output_file.flush()
  nodes_file.close()
  nodes_file_.close()

  ways_file.flush()
  ways_file.seek(0)
  while 1:
    lines = ways_file.readlines(200)
    if not lines:
      break
    else:
      for line in lines:
        output_file.write(line)
        try:
          progress.update(progress.currval + 1)
        except:
          pass
        if random.random() > 0.8 :
          output_file.flush()
  ways_file.close()
  ways_file_.close()

  for rfile in relations_file:
    rfile.flush()
    rfile.seek(0)
    while 1:
      lines = rfile.readlines(200)
      if not lines:
        break
      else:
        for line in lines:
          output_file.write(line)
          try:
            progress.update(progress.currval + 1)
          except:
            pass
          if random.random() > 0.8 :
            output_file.flush()
    rfile.close()

  output_file.write(' </osm>')
  output_file.write('\n')
  output_file.flush()
  output_file.close()
  progress.finish()


#Returns the angle between three points
def angle(p1, p3, p2):
    v1x = p1[0] - p3[0];
    v1y = p1[1] - p3[1];
    v2x = p2[0] - p3[0];
    v2y = p2[1] - p3[1];

    angle = (arctan2([v2y],[v2x]) - arctan2([v1y],[v1x]) );
    angle = angle[0]

    return angle;


if __name__ == "__main__":
  options, remainder = getopt.getopt(sys.argv[1:], 'o:v', ['input-path=', 'help', 'output-file='])
  main(options)

__author__ = 'Maria Arias de Reyna'
