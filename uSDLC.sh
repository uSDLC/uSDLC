#!/bin/bash
#cat nohup.out >> nohup.log
rm nohup.out
nohup java -jar uSDLC-full.jar userId=anon port=80&
tail -f nohup.out
