/**
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
 */
tree grammar MathExpressionEval;

options {
	tokenVocab = MathExpression;
	ASTLabelType = CommonTree;
}

@header {
package ca.gedge.opgraph.nodes.math.parser;

import java.util.HashMap;
}

@members {
	private HashMap<String, Number> values = new HashMap<String, Number>();
	private Double result = null;

	public void putValue(String name, Number value) {
		values.put(name, value);
	}
	
	public Double getResult() {
		return result;
	}
}

prog: expr {this.result = $expr.value;}
    ;

expr returns [double value]
    : ^('*' a=expr b=expr)  {$value = a*b;}
    | ^('/' a=expr b=expr)  {$value = a/b;}
    | ^('%' a=expr b=expr)  {$value = a\%b;}
    | ^('+' a=expr b=expr)  {$value = a+b;}
    | ^('-' a=expr b=expr)  {$value = a-b;}
    | ^(NEGATE a=expr)  {$value = -a;}   
    | ID {
        final Number v = values.get($ID.text);
        if(v == null)
        	throw new NullPointerException("Undefined variable in math expression: " + $ID.text);
        
        $value = v.doubleValue();
    }
    | INT {$value = Integer.parseInt($INT.text);}
    | REAL {$value = Double.parseDouble($REAL.text);}
    ;
