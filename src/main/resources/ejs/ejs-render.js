function nullSafe(value, prop) {
	if (value == null) {
		return value;
	}
	
	var tokens = prop.split('.');
	
	var curr = value;
	
	for (var i = 0; i < tokens.length;  i++) {
		if (typeof curr[tokens[i]] == 'undefined') {
			return null;
		}
		
		curr = curr[tokens[i]];
	}
	
	return curr;
};

ejs.filters.ns = nullSafe;

ejs.filters.date2str = function(date) {
	if (!date || !(date instanceof Date)) {
		return '';
	}
	
	return (date.getDate() <= 9 ? '0' : '') + date.getDate()        + '/' +
		   (date.getMonth() < 9 ? '0' : '')	+ (date.getMonth() + 1) + '/' + 
		   date.getFullYear();
};

ejs.filters.datestr = function(date, separador) {
	if (!date || !(date instanceof Date)) {
		return '';
	}
	
	return date.getFullYear() + separador + 
		(date.getMonth() < 9 ? '0' : '') + (date.getMonth() + 1) + separador + 
		(date.getDate() <= 9 ? '0' : '') + date.getDate();
};

ejs.filters.dateToISO = function toLocalIsoString(date, includeSeconds) {
    function pad(n) { 
    	return n < 10 ? '0' + n : n; 
    }
    var localIsoString = date.getFullYear() + '-'
        + pad(date.getMonth() + 1) + '-'
        + pad(date.getDate()) + 'T'
        + pad(date.getHours()) + ':'
        + pad(date.getMinutes()) + ':'
        + pad(date.getSeconds());
    if(date.getTimezoneOffset() == 0) localIsoString += 'Z';
    return localIsoString;
};

ejs.filters.numf2 = function(number) {
	var decSize = 2;
	var decSep = ',';
	
	if (typeof number != "number") {
		return ejs.filters.numf(0, decSize, decSep);
	}
	
	var intVal = parseInt(number);
	var decVal = parseInt(((1 + (number - intVal).toFixed(2)) * Math.pow(10, decSize)).toFixed(2));
	
    return (intVal+(decVal.toString().substring(0,2)-10)) + (decSize ? (decSep + decVal.toString().substring(2)) : "");
};

ejs.filters.numf = function(number, decSize, decSep) {
	decSize = typeof decSize !== "number" ? 2 : decSize;
	decSep = decSep || ',';
	
	if (typeof number != "number") {
		return ejs.filters.numf(0, decSize, decSep);
	}
	
	var intVal = parseInt(number);
	var decVal = parseInt(((1 + (number - intVal).toFixed(2)) * Math.pow(10, decSize)).toFixed(2));
	
    return (intVal+(decVal.toString().substring(0,2)-10)) + (decSize ? (decSep + decVal.toString().substring(2)) : "");
};

function fromMapToJSObject(value, toNative) {
	var object = {};
	
	for (var i in value) {
		object[i] = toNative(value[i]);
	}

	return object;
}

function toJSRecursive(depth, value) {
	var recursion = function(v) { return v; };
	
	if (depth > 0) {
		recursion = toJSRecursive.bind(null, depth - 1);

	} else if (depth === 0) {
		recursion = toJSRecursive.bind(null, 0);
	}
	
	if (value instanceof Java.type('java.util.Map')) {
		return fromMapToJSObject(value, recursion);
	}
	
	if (value instanceof Java.type('java.util.Collection')) {
		return Java.from(value).map(recursion);
	}
	
	return value;
}

function toJS(value) {
	return toJSRecursive(2, value);
}