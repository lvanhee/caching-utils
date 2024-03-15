module cachingutils {
	exports cachingutils;
	exports cachingutils.advanced.localdatabase;
	exports cachingutils.advanced.failable;
	exports cachingutils.advanced;
	exports cachingutils.advanced.autofilled;
	exports cachingutils.impl;
	exports cachingutils.advanced.tablebased;
	exports cachingutils.parsing;
	
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
}