#!/usr/bin/python

import sys
import getopt
import shapefile
import dbf
import random
import tempfile
import progressbar

def main(argv):
  output_file = None
  shp_path = None
  for opt,arg in argv:
    if opt in ("--input-path"):
      shp_path = arg
    elif opt in ("-o", "--output-file"):
      output_file = open(arg, 'w')
    elif opt in ("-h", "--help"):
      print "Usage NavTeq2OSM.py --i path-to-shapefiles --o output-file"
      
  if output_file is None or shp_path is None:
    print "Usage NavTeq2OSM.py --i path-to-shapefiles --o output-file"
  
  shpfile = shapefile.Reader(shp_path + "/Streets")
  rdms = dbf.Table(shp_path + "/Rdms.dbf")
  zlevels = dbf.Table(shp_path + "/Zlevels.dbf")
  
  id_nodes=0
  all_nodes = {}
  
  nodes_file = tempfile.TemporaryFile()
  ways_file = tempfile.TemporaryFile()
  relations_file = tempfile.TemporaryFile()
  
  max = shpfile.numRecords
  cont = 0
  
  widgets = ['Importing data from NavTeq Shapes: ', progressbar.Bar(marker=progressbar.AnimatedMarker()),
           ' ', progressbar.ETA()]
  maxval = max + len(rdms) + len(zlevels) * 2
  progress = progressbar.ProgressBar(widgets=widgets, maxval=maxval).start()
  
  levels = {}
  
  for i in range(0, len(zlevels)):
    progress.update(i + cont)
    record = zlevels[i]
    
    link_id=record[0]
    level = record[3]
    
    point = record.shape().points[0]
    key = str(point[0]) + "_" + str(point[1])
    
    if levels[key] is None:
      levels[key] = {}
    
    if record[1] == 1:
      levels[str(record['link_id']) + "_f"] = record['z_level']
    else:
      levels[str(record['link_id']) + "_t"] = record['z_level']
      
    
  cont = len(zlevels)
  
  for vertex in levels:
    progress.update(i + cont)
    
    for level in vertex:
      
    
      relations_file.write('  <relation id="' + str(restriction_id) + '" type="restriction" restriction="' + str(restriction_type) + '">\n')
      relations_file.write('    <member type="way" ref="' + str(way_from) + '" role="from"/>\n')
      relations_file.write('    <member type="way" ref="' + str(way_to) + '" role="to"/>\n')
      for n in via:
	relations_file.write('    <member type="way" ref="' + str(n) + '" role="via"/>\n')
      relations_file.write("  </relation>\n")  
      
      
  cont = cont + len(levels)  
  levels = None
  for i in range(0, max):

    progress.update(i + cont)

    shpRecord = shpfile.shapeRecord(i)
    tags={}
    nodes=[]
    attrs={}
    
    attrs['id'] = shpRecord.record[0]
    
    fclass = shpRecord.record[23]
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
    
    tags['name'] = shpRecord.record[1]
    
    dir_travel = shpRecord.record[32]
    if dir_travel == 'F':
      tags['oneway'] = 'yes'
    elif dir_travel == 'T':
      tags['oneway'] = '-1'
    else:
      tags['oneway'] = 'no'
    
    
    #Process Nodes
    
    #Check source and target
    from_point = shpRecord.shape.points[0]
    to_point = shpRecord.shape.points[len(shpRecord.shape.points) - 1]
    to=str(from_point[0]) + "_" + str(from_point[1])
    fr=str(to_point[0]) + "_" + str(to_point[1])
    
    if all_nodes.has_key(to):
      nodes.append(all_nodes.get(to))
    else:
      x = from_point[0]
      y = from_point[1]
      all_nodes[to] = id_nodes
      n_attrs = {}
      
      n_attrs['id'] = id_nodes
      n_attrs['lon'] = x
      n_attrs['lat'] = y
      
      nodes.append(id_nodes)
      
      #Write new node to file
      nodes_file.write("  <node")
      for k in n_attrs:
	nodes_file.write(" " + str(k) + "=" + str(n_attrs.get(k)))
      nodes_file.write("/>\n")
      
      id_nodes = id_nodes + 1
      
    
    i = 1
    while i < len(shpRecord.shape.points) - 2:
      point = shpRecord.shape.points[i]
      x = point[0]
      y = point[1]
      n_attrs = {}
      
      n_attrs['id'] = id_nodes
      n_attrs['lon'] = x
      n_attrs['lat'] = y
      
      nodes.append(id_nodes)
      
      #Write new node to file
      nodes_file.write("  <node")
      for k in n_attrs:
	nodes_file.write(" " + str(k) + "=" + str(n_attrs.get(k)))
      nodes_file.write("/>\n")
      
      id_nodes = id_nodes + 1
      
      i = i + 1
    
    if all_nodes.has_key(fr):
      nodes.append(all_nodes.get(fr))
    else:
      all_nodes[fr] = id_nodes
      x = to_point[0]
      y = to_point[1]
      n_attrs = {}
      
      n_attrs['id'] = id_nodes
      n_attrs['lon'] = x
      n_attrs['lat'] = y
      
      nodes.append(id_nodes)
      
      #Write new node to file
      nodes_file.write("  <node")
      for k in n_attrs:
	nodes_file.write(" " + str(k) + "=" + str(n_attrs.get(k)))
      nodes_file.write("/>\n")
      
      id_nodes = id_nodes + 1
    
    #Write way to file
    ways_file.write("  <way")
    for k in attrs:
      ways_file.write(" " + str(k) + "=" + str(attrs.get(k)))
    ways_file.write(">\n")
    for n in nodes:
      ways_file.write("     <nd id=" + str(n) + "/>\n")
    for k in tags:
      ways_file.write("     <tag " + str(k) + "=" + str(tags.get(k)) + "/>\n")
    ways_file.write("  </way>\n")
    
      
  nodes = None
  via = []
  restriction_type = None
  way_from = None
  way_to = None
  restriction_id = None
  
  cont = cont + max
  for i in range(0, len(rdms)):
    progress.update(i + cont)
    record = rdms[i]
    
    if record[3] == 1 :
      if restriction_type is not None:
	relations_file.write('  <relation id="' + str(restriction_id) + '" type="restriction" restriction="' + str(restriction_type) + '">\n')
	relations_file.write('    <member type="way" ref="' + str(way_from) + '" role="from"/>\n')
	relations_file.write('    <member type="way" ref="' + str(way_to) + '" role="to"/>\n')
	for n in via:
	  relations_file.write('    <member type="way" ref="' + str(n) + '" role="via"/>\n')
	relations_file.write("  </relation>\n")
	
      #Generating new restriction
      via = []
      #TODO:
      restriction_type = "no_right_turn"
      way_from = record[0]
      way_to = record[2]
      restriction_id = record[1]
      
    else:
      via.append(record[2])
      way_to = record[2]
      
  if restriction_type is not None:
    relations_file.write('  <relation id="' + str(restriction_id) + '" type="restriction" restriction="' + str(restriction_type) + '">')
    relations_file.write('    <member type="way" ref="' + str(way_from) + '" role="from"/>')
    relations_file.write('    <member type="way" ref="' + str(way_to) + '" role="to"/>')
    
    for n in via:
      relations_file.write('    <member type="way" ref="' + str(n) + '" role="via"/>')
    
    relations_file.write("  </relation>\n")
    
   
  progress.finish()
  print "Writing data to file .osm"
  
  
  output_file.write("<?xml version='1.0' encoding='UTF-8'?>")
  output_file.write('\n')
  output_file.write(" <osm version='0.6' generator='navteq2osm'>")
  output_file.write('\n')
  
  nodes_file.seek(0)
  for line in nodes_file:
    output_file.write(line)
    
  ways_file.seek(0)
  for line in ways_file:
    output_file.write(line)
    
  relations_file.seek(0)
  for line in relations_file:
    output_file.write(line)
  output_file.write(' </osm>')
  
  output_file.close()
  nodes_file.close()
  ways_file.close()
  relations_file.close()
      
if __name__ == "__main__":
  options, remainder = getopt.getopt(sys.argv[1:], 'o:v', ['input-path=', 'help', 'output-file='])
  main(options)
