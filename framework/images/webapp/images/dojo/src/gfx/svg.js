/*
	Copyright (c) 2004-2006, The Dojo Foundation
	All Rights Reserved.

	Licensed under the Academic Free License version 2.1 or above OR the
	modified BSD license. For more information on Dojo licensing, see:

		http://dojotoolkit.org/community/licensing.shtml
*/

dojo.provide("dojo.gfx.svg");

dojo.require("dojo.lang.declare");
dojo.require("dojo.svg");

dojo.require("dojo.gfx.color");
dojo.require("dojo.gfx.common");
dojo.require("dojo.gfx.shape");
dojo.require("dojo.gfx.path");

dojo.require("dojo.experimental");
dojo.experimental("dojo.gfx.svg");

dojo.gfx.svg.getRef = function(fill){
	// summary: returns a DOM Node specified by the fill argument or null
	// fill: String: an SVG fill
	if(!fill || fill == "none") return null;
	if(fill.match(/^url\(#.+\)$/)){
		return dojo.byId(fill.slice(5, -1));	// Node
	}
	// Opera's bug: incorrect representation of a reference
	if(dojo.render.html.opera && fill.match(/^#dj_unique_.+$/)){
		// we assume here that a reference was generated by dojo.gfx
		return dojo.byId(fill.slice(1));	// Node
	}
	return null;	// Node
};

dojo.lang.extend(dojo.gfx.Shape, {
	// summary: SVG-specific implementation of dojo.gfx.Shape methods

	setFill: function(fill){
		// summary: sets a fill object (SVG)
		// fill: Object: a fill object
		//	(see dojo.gfx.defaultLinearGradient,
		//	dojo.gfx.defaultRadialGradient,
		//	dojo.gfx.defaultPattern,
		//	or dojo.gfx.color.Color)

		if(!fill){
			// don't fill
			this.fillStyle = null;
			this.rawNode.setAttribute("fill", "none");
			this.rawNode.setAttribute("fill-opacity", 0);
			return this;
		}
		if(typeof(fill) == "object" && "type" in fill){
			// gradient
			switch(fill.type){
				case "linear":
					var f = dojo.gfx.makeParameters(dojo.gfx.defaultLinearGradient, fill);
					var gradient = this._setFillObject(f, "linearGradient");
					dojo.lang.forEach(["x1", "y1", "x2", "y2"], function(x){
						gradient.setAttribute(x, f[x].toFixed(8));
					});
					break;
				case "radial":
					var f = dojo.gfx.makeParameters(dojo.gfx.defaultRadialGradient, fill);
					var gradient = this._setFillObject(f, "radialGradient");
					dojo.lang.forEach(["cx", "cy", "r"], function(x){
						gradient.setAttribute(x, f[x].toFixed(8));
					});
					break;
				case "pattern":
					var f = dojo.gfx.makeParameters(dojo.gfx.defaultPattern, fill);
					var pattern = this._setFillObject(f, "pattern");
					dojo.lang.forEach(["x", "y", "width", "height"], function(x){
						pattern.setAttribute(x, f[x].toFixed(8));
					});
					break;
			}
			return this;
		}
		// color object
		var f = dojo.gfx.normalizeColor(fill);
		this.fillStyle = f;
		this.rawNode.setAttribute("fill", f.toCss());
		this.rawNode.setAttribute("fill-opacity", f.a);
		return this;	// self
	},

	setStroke: function(stroke){
		// summary: sets a stroke object (SVG)
		// stroke: Object: a stroke object
		//	(see dojo.gfx.defaultStroke)

		if(!stroke){
			// don't stroke
			this.strokeStyle = null;
			this.rawNode.setAttribute("stroke", "none");
			this.rawNode.setAttribute("stroke-opacity", 0);
			return this;
		}
		// normalize the stroke
		this.strokeStyle = dojo.gfx.makeParameters(dojo.gfx.defaultStroke, stroke);
		this.strokeStyle.color = dojo.gfx.normalizeColor(this.strokeStyle.color);
		// generate attributes
		var s = this.strokeStyle;
		var rn = this.rawNode;
		if(s){
			rn.setAttribute("stroke", s.color.toCss());
			rn.setAttribute("stroke-opacity", s.color.a);
			rn.setAttribute("stroke-width",   s.width);
			rn.setAttribute("stroke-linecap", s.cap);
			if(typeof(s.join) == "number"){
				rn.setAttribute("stroke-linejoin",   "miter");
				rn.setAttribute("stroke-miterlimit", s.join);
			}else{
				rn.setAttribute("stroke-linejoin",   s.join);
			}
		}
		return this;	// self
	},

	_setFillObject: function(f, nodeType){
		var def_elems = this.rawNode.parentNode.getElementsByTagName("defs");
		if(def_elems.length == 0){ return this; }
		this.fillStyle = f;
		var defs = def_elems[0];
		var fill = this.rawNode.getAttribute("fill");
		var ref  = dojo.gfx.svg.getRef(fill);
		if(ref){
			fill = ref;
			if(fill.tagName.toLowerCase() != nodeType.toLowerCase()){
				var id = fill.id;
				fill.parentNode.removeChild(fill);
				fill = document.createElementNS(dojo.svg.xmlns.svg, nodeType);
				fill.setAttribute("id", id);
				defs.appendChild(fill);
			}else{
				while(fill.childNodes.length){
					fill.removeChild(fill.lastChild);
				}
			}
		}else{
			fill = document.createElementNS(dojo.svg.xmlns.svg, nodeType);
			fill.setAttribute("id", dojo.dom.getUniqueId());
			defs.appendChild(fill);
		}
		if(nodeType == "pattern"){
			fill.setAttribute("patternUnits", "userSpaceOnUse");
			var img = document.createElementNS(dojo.svg.xmlns.svg, "image");
			img.setAttribute("x", 0);
			img.setAttribute("y", 0);
			img.setAttribute("width",  f.width .toFixed(8));
			img.setAttribute("height", f.height.toFixed(8));
			img.setAttributeNS(dojo.svg.xmlns.xlink, "href", f.src);
			fill.appendChild(img);
		}else{
			fill.setAttribute("gradientUnits", "userSpaceOnUse");
			for(var i = 0; i < f.colors.length; ++i){
				f.colors[i].color = dojo.gfx.normalizeColor(f.colors[i].color);
				var t = document.createElementNS(dojo.svg.xmlns.svg, "stop");
				t.setAttribute("offset",     f.colors[i].offset.toFixed(8));
				t.setAttribute("stop-color", f.colors[i].color.toCss());
				fill.appendChild(t);
			}
		}
		this.rawNode.setAttribute("fill", "url(#" + fill.getAttribute("id") +")");
		this.rawNode.removeAttribute("fill-opacity");
		return fill;
	},

	_applyTransform: function() {
		var matrix = this._getRealMatrix();
		if(matrix){
			var tm = this.matrix;
			this.rawNode.setAttribute("transform", "matrix(" +
				tm.xx.toFixed(8) + "," + tm.yx.toFixed(8) + "," +
				tm.xy.toFixed(8) + "," + tm.yy.toFixed(8) + "," +
				tm.dx.toFixed(8) + "," + tm.dy.toFixed(8) + ")");
		}else{
			this.rawNode.removeAttribute("transform");
		}
		return this;
	},

	setRawNode: function(rawNode){
		// summary:
		//	assigns and clears the underlying node that will represent this
		//	shape. Once set, transforms, gradients, etc, can be applied.
		//	(no fill & stroke by default)
		with(rawNode){
			setAttribute("fill", "none");
			setAttribute("fill-opacity", 0);
			setAttribute("stroke", "none");
			setAttribute("stroke-opacity", 0);
			setAttribute("stroke-width", 1);
			setAttribute("stroke-linecap", "butt");
			setAttribute("stroke-linejoin", "miter");
			setAttribute("stroke-miterlimit", 4);
		}
		this.rawNode = rawNode;
	},

	moveToFront: function(){
		// summary: moves a shape to front of its parent's list of shapes (SVG)
		this.rawNode.parentNode.appendChild(this.rawNode);
		return this;	// self
	},
	moveToBack: function(){
		// summary: moves a shape to back of its parent's list of shapes (SVG)
		this.rawNode.parentNode.insertBefore(this.rawNode, this.rawNode.parentNode.firstChild);
		return this;	// self
	},

	setShape: function(newShape){
		// summary: sets a shape object (SVG)
		// newShape: Object: a shape object
		//	(see dojo.gfx.defaultPath,
		//	dojo.gfx.defaultPolyline,
		//	dojo.gfx.defaultRect,
		//	dojo.gfx.defaultEllipse,
		//	dojo.gfx.defaultCircle,
		//	dojo.gfx.defaultLine,
		//	or dojo.gfx.defaultImage)
		this.shape = dojo.gfx.makeParameters(this.shape, newShape);
		for(var i in this.shape){
			if(i != "type"){ this.rawNode.setAttribute(i, this.shape[i]); }
		}
		return this;	// self
	},

	attachFill: function(rawNode){
		// summary: deduces a fill style from a Node.
		// rawNode: Node: an SVG node
		var fillStyle = null;
		if(rawNode){
			var fill = rawNode.getAttribute("fill");
			if(fill == "none"){ return; }
			var ref  = dojo.gfx.svg.getRef(fill);
			if(ref){
				var gradient = ref;
				switch(gradient.tagName.toLowerCase()){
					case "lineargradient":
						fillStyle = this._getGradient(dojo.gfx.defaultLinearGradient, gradient);
						dojo.lang.forEach(["x1", "y1", "x2", "y2"], function(x){
							fillStyle[x] = gradient.getAttribute(x);
						});
						break;
					case "radialgradient":
						fillStyle = this._getGradient(dojo.gfx.defaultRadialGradient, gradient);
						dojo.lang.forEach(["cx", "cy", "r"], function(x){
							fillStyle[x] = gradient.getAttribute(x);
						});
						fillStyle.cx = gradient.getAttribute("cx");
						fillStyle.cy = gradient.getAttribute("cy");
						fillStyle.r  = gradient.getAttribute("r");
						break;
					case "pattern":
						fillStyle = dojo.lang.shallowCopy(dojo.gfx.defaultPattern, true);
						dojo.lang.forEach(["x", "y", "width", "height"], function(x){
							fillStyle[x] = gradient.getAttribute(x);
						});
						fillStyle.src = gradient.firstChild.getAttributeNS(dojo.svg.xmlns.xlink, "href");
						break;
				}
			}else{
				fillStyle = new dojo.gfx.color.Color(fill);
				var opacity = rawNode.getAttribute("fill-opacity");
				if(opacity != null) fillStyle.a = opacity;
			}
		}
		return fillStyle;	// Object
	},

	_getGradient: function(defaultGradient, gradient){
		var fillStyle = dojo.lang.shallowCopy(defaultGradient, true);
		fillStyle.colors = [];
		for(var i = 0; i < gradient.childNodes.length; ++i){
			fillStyle.colors.push({
				offset: gradient.childNodes[i].getAttribute("offset"),
				color:  new dojo.gfx.color.Color(gradient.childNodes[i].getAttribute("stop-color"))
			});
		}
		return fillStyle;
	},

	attachStroke: function(rawNode){
		// summary: deduces a stroke style from a Node.
		// rawNode: Node: an SVG node
		if(!rawNode){ return; }
		var stroke = rawNode.getAttribute("stroke");
		if(stroke == null || stroke == "none") return null;
		var strokeStyle = dojo.lang.shallowCopy(dojo.gfx.defaultStroke, true);
		var color = new dojo.gfx.color.Color(stroke);
		if(color){
			strokeStyle.color = color;
			strokeStyle.color.a = rawNode.getAttribute("stroke-opacity");
			strokeStyle.width = rawNode.getAttribute("stroke-width");
			strokeStyle.cap = rawNode.getAttribute("stroke-linecap");
			strokeStyle.join = rawNode.getAttribute("stroke-linejoin");
			if(strokeStyle.join == "miter"){
				strokeStyle.join = rawNode.getAttribute("stroke-miterlimit");
			}
		}
		return strokeStyle;	// Object
	},

	attachTransform: function(rawNode){
		// summary: deduces a transformation matrix from a Node.
		// rawNode: Node: an SVG node
		var matrix = null;
		if(rawNode){
			matrix = rawNode.getAttribute("transform");
			if(matrix.match(/^matrix\(.+\)$/)){
				var t = matrix.slice(7, -1).split(",");
				matrix = dojo.gfx.matrix.normalize({
					xx: parseFloat(t[0]), xy: parseFloat(t[2]),
					yx: parseFloat(t[1]), yy: parseFloat(t[3]),
					dx: parseFloat(t[4]), dy: parseFloat(t[5])
				});
			}
		}
		return matrix;	// dojo.gfx.matrix.Matrix
	},

	attachShape: function(rawNode){
		// summary: builds a shape from a Node.
		// rawNode: Node: an SVG node
		var shape = null;
		if(rawNode){
			shape = dojo.lang.shallowCopy(this.shape, true);
			for(var i in shape) {
				shape[i] = rawNode.getAttribute(i);
			}
		}
		return shape;	// dojo.gfx.Shape
	},

	attach: function(rawNode){
		// summary: reconstructs all shape parameters from a Node.
		// rawNode: Node: an SVG node
		if(rawNode) {
			this.rawNode = rawNode;
			this.fillStyle = this.attachFill(rawNode);
			this.strokeStyle = this.attachStroke(rawNode);
			this.matrix = this.attachTransform(rawNode);
			this.shape = this.attachShape(rawNode);
		}
	}
});

dojo.declare("dojo.gfx.Group", dojo.gfx.Shape, {
	// summary: a group shape (SVG), which can be used
	//	to logically group shapes (e.g, to propagate matricies)

	setRawNode: function(rawNode){
		// summary: sets a raw SVG node to be used by this shape
		// rawNode: Node: an SVG node
		this.rawNode = rawNode;
	}
});
dojo.gfx.Group.nodeType = "g";

dojo.declare("dojo.gfx.Rect", dojo.gfx.shape.Rect, {
	// summary: a rectangle shape (SVG)

	attachShape: function(rawNode){
		// summary: builds a rectangle shape from a Node.
		// rawNode: Node: an SVG node
		var shape = null;
		if(rawNode){
			shape = dojo.gfx.Rect.superclass.attachShape.apply(this, arguments);
			shape.r = Math.min(rawNode.getAttribute("rx"), rawNode.getAttribute("ry"));
		}
		return shape;	// dojo.gfx.shape.Rect
	},
	setShape: function(newShape){
		// summary: sets a rectangle shape object (SVG)
		// newShape: Object: a rectangle shape object
		this.shape = dojo.gfx.makeParameters(this.shape, newShape);
		this.bbox = null;
		for(var i in this.shape){
			if(i != "type" && i != "r"){ this.rawNode.setAttribute(i, this.shape[i]); }
		}
		this.rawNode.setAttribute("rx", this.shape.r);
		this.rawNode.setAttribute("ry", this.shape.r);
		return this;	// self
	}
});
dojo.gfx.Rect.nodeType = "rect";

dojo.gfx.Ellipse = dojo.gfx.shape.Ellipse;
dojo.gfx.Ellipse.nodeType = "ellipse";

dojo.gfx.Circle = dojo.gfx.shape.Circle;
dojo.gfx.Circle.nodeType = "circle";

dojo.gfx.Line = dojo.gfx.shape.Line;
dojo.gfx.Line.nodeType = "line";

dojo.declare("dojo.gfx.Polyline", dojo.gfx.shape.Polyline, {
	// summary: a polyline/polygon shape (SVG)

	setShape: function(points){
		// summary: sets a polyline/polygon shape object (SVG)
		// points: Object: a polyline/polygon shape object
		if(points && points instanceof Array){
			// branch
			// points: Array: an array of points
			this.shape = dojo.gfx.makeParameters(this.shape, { points: points });
			if(closed && this.shape.points.length){
				this.shape.points.push(this.shape.points[0]);
			}
		}else{
			this.shape = dojo.gfx.makeParameters(this.shape, points);
		}
		this.box = null;
		var attr = [];
		var p = this.shape.points;
		for(var i = 0; i < p.length; ++i){
			attr.push(p[i].x.toFixed(8));
			attr.push(p[i].y.toFixed(8));
		}
		this.rawNode.setAttribute("points", attr.join(" "));
		return this;	// self
	}
});
dojo.gfx.Polyline.nodeType = "polyline";

dojo.declare("dojo.gfx.Image", dojo.gfx.shape.Image, {
	// summary: an image (SVG)

	setShape: function(newShape){
		// summary: sets an image shape object (SVG)
		// newShape: Object: an image shape object
		this.shape = dojo.gfx.makeParameters(this.shape, newShape);
		this.bbox = null;
		var rawNode = this.rawNode;
		for(var i in this.shape){
			if(i != "type" && i != "src"){ rawNode.setAttribute(i, this.shape[i]); }
		}
		rawNode.setAttributeNS(dojo.svg.xmlns.xlink, "href", this.shape.src);
		return this;	// self
	},
	setStroke: function(){
		// summary: ignore setting a stroke style
		return this;	// self
	},
	setFill: function(){
		// summary: ignore setting a fill style
		return this;	// self
	},
	attachStroke: function(rawNode){
		// summary: ignore attaching a stroke style
		return null;
	},
	attachFill: function(rawNode){
		// summary: ignore attaching a fill style
		return null;
	}
});
dojo.gfx.Image.nodeType = "image";

dojo.declare("dojo.gfx.Path", dojo.gfx.path.Path, {
	// summary: a path shape (SVG)

	_updateWithSegment: function(segment){
		// summary: updates the bounding box of path with new segment
		// segment: Object: a segment
		dojo.gfx.Path.superclass._updateWithSegment.apply(this, arguments);
		if(typeof(this.shape.path) == "string"){
			this.rawNode.setAttribute("d", this.shape.path);
		}
	},
	setShape: function(newShape){
		// summary: forms a path using a shape (SVG)
		// newShape: Object: an SVG path string or a path object (see dojo.gfx.defaultPath)
		dojo.gfx.Path.superclass.setShape.apply(this, arguments);
		this.rawNode.setAttribute("d", this.shape.path);
		return this;	// self
	}
});
dojo.gfx.Path.nodeType = "path";

dojo.gfx._creators = {
	// summary: SVG shape creators
	createPath: function(path){
		// summary: creates an SVG path shape
		// path: Object: a path object (see dojo.gfx.defaultPath)
		return this.createObject(dojo.gfx.Path, path);	// dojo.gfx.Path
	},
	createRect: function(rect){
		// summary: creates an SVG rectangle shape
		// rect: Object: a path object (see dojo.gfx.defaultRect)
		return this.createObject(dojo.gfx.Rect, rect);	// dojo.gfx.Rect
	},
	createCircle: function(circle){
		// summary: creates an SVG circle shape
		// circle: Object: a circle object (see dojo.gfx.defaultCircle)
		return this.createObject(dojo.gfx.Circle, circle);	// dojo.gfx.Circle
	},
	createEllipse: function(ellipse){
		// summary: creates an SVG ellipse shape
		// ellipse: Object: an ellipse object (see dojo.gfx.defaultEllipse)
		return this.createObject(dojo.gfx.Ellipse, ellipse);	// dojo.gfx.Ellipse
	},
	createLine: function(line){
		// summary: creates an SVG line shape
		// line: Object: a line object (see dojo.gfx.defaultLine)
		return this.createObject(dojo.gfx.Line, line);	// dojo.gfx.Line
	},
	createPolyline: function(points){
		// summary: creates an SVG polyline/polygon shape
		// points: Object: a points object (see dojo.gfx.defaultPolyline)
		//	or an Array of points
		return this.createObject(dojo.gfx.Polyline, points);	// dojo.gfx.Polyline
	},
	createImage: function(image){
		// summary: creates an SVG image shape
		// image: Object: an image object (see dojo.gfx.defaultImage)
		return this.createObject(dojo.gfx.Image, image);	// dojo.gfx.Image
	},
	createGroup: function(){
		// summary: creates an SVG group shape
		return this.createObject(dojo.gfx.Group);	// dojo.gfx.Group
	},
	createObject: function(shapeType, rawShape){
		// summary: creates an instance of the passed shapeType class
		// shapeType: Function: a class constructor to create an instance of
		// rawShape: Object: properties to be passed in to the classes "setShape" method
		if(!this.rawNode){ return null; }
		var shape = new shapeType();
		var node = document.createElementNS(dojo.svg.xmlns.svg, shapeType.nodeType);
		shape.setRawNode(node);
		this.rawNode.appendChild(node);
		shape.setShape(rawShape);
		this.add(shape);
		return shape;	// dojo.gfx.Shape
	},
	// group control
	add: function(shape){
		// summary: adds a shape to a group/surface
		// shape: dojo.gfx.Shape: an SVG shape object
		var oldParent = shape.getParent();
		if(oldParent){
			oldParent.remove(shape, true);
		}
		shape._setParent(this, null);
		this.rawNode.appendChild(shape.rawNode);
		return this;	// self
	},
	remove: function(shape, silently){
		// summary: remove a shape from a group/surface
		// shape: dojo.gfx.Shape: an SVG shape object
		// silently: Boolean?: if true, regenerate a picture
		if(this.rawNode == shape.rawNode.parentNode){
			this.rawNode.removeChild(shape.rawNode);
		}
		shape._setParent(null, null);
		return this;	// self
	}
};

dojo.gfx.attachNode = function(node){
	// summary: creates a shape from a Node
	// node: Node: an SVG node
	if(!node) return null;
	var s = null;
	switch(node.tagName.toLowerCase()){
		case dojo.gfx.Rect.nodeType:
			s = new dojo.gfx.Rect();
			break;
		case dojo.gfx.Ellipse.nodeType:
			s = new dojo.gfx.Ellipse();
			break;
		case dojo.gfx.Polyline.nodeType:
			s = new dojo.gfx.Polyline();
			break;
		case dojo.gfx.Path.nodeType:
			s = new dojo.gfx.Path();
			break;
		case dojo.gfx.Circle.nodeType:
			s = new dojo.gfx.Circle();
			break;
		case dojo.gfx.Line.nodeType:
			s = new dojo.gfx.Line();
			break;
		case dojo.gfx.Image.nodeType:
			s = new dojo.gfx.Image();
			break;
		default:
			dojo.debug("FATAL ERROR! tagName = " + node.tagName);
	}
	s.attach(node);
	return s;	// dojo.gfx.Shape
};

dojo.lang.extend(dojo.gfx.Surface, {
	// summary: a surface object to be used for drawings (SVG)

	setDimensions: function(width, height){
		// summary: sets the width and height of the rawNode
		// width: String: width of surface, e.g., "100px"
		// height: String: height of surface, e.g., "100px"
		if(!this.rawNode){ return this; }
		this.rawNode.setAttribute("width",  width);
		this.rawNode.setAttribute("height", height);
		return this;	// self
	},
	getDimensions: function(){
		// summary: returns an object with properties "width" and "height"
		return this.rawNode ? {width: this.rawNode.getAttribute("width"), height: this.rawNode.getAttribute("height")} : null; // Object
	}
});

dojo.gfx.createSurface = function(parentNode, width, height){
	// summary: creates a surface (SVG)
	// parentNode: Node: a parent node
	// width: String: width of surface, e.g., "100px"
	// height: String: height of surface, e.g., "100px"

	var s = new dojo.gfx.Surface();
	s.rawNode = document.createElementNS(dojo.svg.xmlns.svg, "svg");
	s.rawNode.setAttribute("width",  width);
	s.rawNode.setAttribute("height", height);

	var defs = new dojo.gfx.svg.Defines();
	var node = document.createElementNS(dojo.svg.xmlns.svg, dojo.gfx.svg.Defines.nodeType);
	defs.setRawNode(node);
	s.rawNode.appendChild(node);

	dojo.byId(parentNode).appendChild(s.rawNode);
	return s;	// dojo.gfx.Surface
};

dojo.gfx.attachSurface = function(node){
	// summary: creates a surface from a Node
	// node: Node: an SVG node
	var s = new dojo.gfx.Surface();
	s.rawNode = node;
	return s;	// dojo.gfx.Surface
};

dojo.lang.extend(dojo.gfx.Group, dojo.gfx._creators);
dojo.lang.extend(dojo.gfx.Surface, dojo.gfx._creators);

delete dojo.gfx._creators;

// Gradient and pattern

dojo.gfx.svg.Defines = function(){
	this.rawNode = null;
};
dojo.lang.extend(dojo.gfx.svg.Defines, {
	setRawNode: function(rawNode){
		this.rawNode = rawNode;
	}
});
dojo.gfx.svg.Defines.nodeType = "defs";
