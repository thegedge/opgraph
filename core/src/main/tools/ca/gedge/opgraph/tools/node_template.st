<!
 * Copyright (C) 2012 Jason Gedge <http://www.gedge.ca>
 *
 * This file is part of the OpGraph project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 !>
group XML;

base(node) ::= <<
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE topic PUBLIC "-//OASIS//DTD DITA Topic//EN" "topic.dtd">
<topic id="topic">
	<title><cmdname id="name">$node.name$</cmdname></title>
	<body>
	<p id="description">$node.description$</p>
	<p>
		<b>Input(s)</b>
		<ul id="inputs">
			$node.inputFields:inputField()$		</ul>
		<b>Output(s)</b>
		<ul id="outputs">
			$node.outputFields:outputField()$		</ul>
	</p>
  </body>
</topic>
>>

inputField(field) ::= <<
<li><varname>$field.key$</varname><i>$field.description$</i></li>

>>

outputField(field) ::= <<
<li><varname>$field.key$</varname><i>$field.description$</i></li>

>>
