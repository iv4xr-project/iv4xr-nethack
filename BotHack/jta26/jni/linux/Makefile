# This file is part of "JTA - Telnet/SSH for the JAVA(tm) platform".
#
# (c) Matthias L. Jugel, Marcus Meißner 1996-2005. All Rights Reserved.
#
# Please visit http://javatelnet.org/ for updates and contact.
#
# --LICENSE NOTICE--
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
# --LICENSE NOTICE--
#

JAVAH	=	javah
TOPDIR	=	../..
SRCDIR	=	../../de
CDIR	=	../src
 
JNI_INCLUDE =	-I/usr/lib/j2sdk1.3/include -I/usr/lib/j2sdk1.3/include/linux -I/usr/lib/jvm/java-8-openjdk-amd64/include -I/usr/lib/jvm/java-8-openjdk-amd64/include/linux

# Linux (glibc 2.1.3)
# libutil contains forkpty()
CC	=	gcc -Ofast
SOCFLAGS =	-fPIC -c -Wall -I. $(JNI_INCLUDE)
SOLFLAGS =	-shared -lutil

#
# major rules to create files
#
all: 	libjtapty.so

HandlerPTY.h:  $(SRCDIR)/mud/jta/plugin/HandlerPTY.class
	$(JAVAH) -classpath /usr/java/lib/classes.zip:$(TOPDIR) -o $@ de.mud.jta.plugin.HandlerPTY

libjtapty.so: HandlerPTY.lo
	$(CC) $(SOLFLAGS) -o $@ HandlerPTY.lo

HandlerPTY.lo: $(CDIR)/HandlerPTY.c HandlerPTY.h
	$(CC) $(SOCFLAGS) -o $@ $(CDIR)/HandlerPTY.c

clean:
	-find . -name \*~ -print | xargs rm -f
	-rm -f HandlerPTY.h HandlerPTY.lo libjtapty.so

realclean: clean

# EOF
