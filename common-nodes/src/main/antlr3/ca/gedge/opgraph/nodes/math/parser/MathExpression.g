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
grammar MathExpression;

options {
	output = AST;
    ASTLabelType = CommonTree;
}

tokens {
	NEGATE; // imaginary token
}

//---------------------------------------------------------------------

@lexer::header {
package ca.gedge.opgraph.nodes.math.parser;
}

@parser::header {
package ca.gedge.opgraph.nodes.math.parser;

import java.util.Set;
import java.util.TreeSet;
}

@parser::members {
	private TreeSet<String> variables = new TreeSet<String>();
	
	public Set<String> getVariables() {
		return variables;
	}
	
	@Override
	public void reportError(RecognitionException e) {
		++state.syntaxErrors;
	}
}

//---------------------------------------------------------------------

prog: expr -> expr
    | EOF  ->
    ;

expr: expr_mdm (('+'^ | '-'^) expr_mdm)*
       ;

expr_mdm: expr_n (('*'^ | '/'^ | '%'^) expr_n)*
        ;
        
expr_n: '-' value -> ^(NEGATE["-"] value)
      | value
      ;
      
value: INT 
     | REAL
     | ID {variables.add($ID.text);}
     | '('! expr ')'!
     ;

//---------------------------------------------------------------------

WS   : (' ' | '\t')+ {skip();};

ID   : LETTER (LETTER | DIGIT | '_')*;
INT  : '-'? DIGIT+;
REAL : INT '.' DIGIT+;

fragment LETTER : ('a'..'z' | 'A'..'Z');
fragment DIGIT  : '0'..'9';
