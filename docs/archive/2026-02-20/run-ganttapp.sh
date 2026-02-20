#!/bin/bash
# Script to run GanttApp with explicit configuration properties

cd /home/r-uu/develop/github/main/root/app/jeeeraaah/frontend/ui/fx

mvn clean compile

mvn exec:java \
  -Dexec.mainClass="de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt.GanttAppRunner" \
  -Dkeycloak.test.user=test \
  -Dkeycloak.test.password=test \
  -Dtesting=true

