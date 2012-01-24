#!usr/bin/python
# Filename: plpy.py

version = '0.1'

#This module allows us to use postgres pl/python functions outside postgres.

#By executing this module you give me your soul and also accept that this code comes with no warranty.
#The real intention of this piece of code is, in fact, to destroy your computer and kill your cat, if any.

#You have been warned

import psycopg2
import psycopg2.extras
import psycopg2.extensions
import time

#psycopg2.extensions.register_type(psycopg2.extensions.UNICODE)
#psycopg2.extensions.register_type(psycopg2.extensions.UNICODEARRAY)

conn = None
seed = 0

def resetConnection():
    global conn
    if conn is not None:
        conn.rollback()
        conn.close()
    conn = psycopg2.connect(host="gofre", port="5433", database="gofleetls-teleatlas", user="gofleetls", password="gofleetls")

def info(*args):
    info = ""
    for arg in args:
        info = info + str(arg) + ", "
    print info

def prepare(sql, parameters):
    global conn
    if conn is None:
        resetConnection()
    cur = conn.cursor()
    plan =randomname()
    sql_ = 'PREPARE ' + plan + ' (' + printlist(parameters) + ') AS ' + str(sql) + ';'
#    print sql_
    cur.execute(sql_)
#    print cur.statusmessage
    cur.close()
    return plan

def execute(plan, parameters, limit=5000):
    global conn
    if conn is None:
        resetConnection()
    cur = conn.cursor(cursor_factory=psycopg2.extras.DictCursor)
    sql_ = 'EXECUTE ' + str(plan) + '(' + printlist(parameters, 1) + ');'
#    print sql_
#    print printlist(parameters)
    cur.execute(sql_)
    res = cur.fetchmany(limit)
#    print cur.statusmessage
    cur.close()
    return res

def printlist(list, comillas=0):
    res = []
    if comillas :
        for elem in list:
            res.append(psycopg2.extensions.adapt(elem).getquoted())
        list = res
    res = ""
    for elem in list:
        if len(res) :
            res = res + ", "
        res = res + str(elem)
    return res

def randomname():
    global seed
    seed = seed + 1
    return "plan_" + str(seed) + "_" + str(time.strftime("%s"))

