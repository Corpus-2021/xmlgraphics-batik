/*

   Copyright 2000-2002  The Apache Software Foundation 

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.batik.dom.svg;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedNumber;

/**
 * This class implements the {@link SVGAnimatedNumber} interface.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id$
 */
public class SVGOMAnimatedNumber
        extends AbstractSVGAnimatedValue
        implements SVGAnimatedNumber {

    /**
     * The default value.
     */
    protected float defaultValue;

    /**
     * Whether the base value is valid.
     */
    protected boolean valid;

    /**
     * The current base value.
     */
    protected float baseVal;

    /**
     * The current animated value.
     */
    protected float animVal;

    /**
     * Whether the value is changing.
     */
    protected boolean changing;

    /**
     * Creates a new SVGOMAnimatedNumber.
     * @param elt The associated element.
     * @param ns The attribute's namespace URI.
     * @param ln The attribute's local name.
     * @param val The default value, if the attribute is not specified.
     */
    public SVGOMAnimatedNumber(AbstractElement elt,
                               String ns,
                               String ln,
                               float  val) {
        super(elt, ns, ln);
        defaultValue = val;
    }

    /**
     * <b>DOM</b>: Implements {@link SVGAnimatedNumber#getBaseVal()}.
     */
    public float getBaseVal() {
        if (!valid) {
            update();
        }
        return baseVal;
    }

    /**
     * Updates the base value from the attribute.
     */
    protected void update() {
        Attr attr = element.getAttributeNodeNS(namespaceURI, localName);
        if (attr == null) {
            baseVal = defaultValue;
        } else {
            baseVal = Float.parseFloat(attr.getValue());
        }
        valid = true;
    }

    /**
     * <b>DOM</b>: Implements {@link SVGAnimatedNumber#setBaseVal(float)}.
     */
    public void setBaseVal(float baseVal) throws DOMException {
        try {
            this.baseVal = baseVal;
            valid = true;
            changing = true;
            element.setAttributeNS(namespaceURI, localName,
                                   String.valueOf(baseVal));
        } finally {
            changing = false;
        }
    }

    /**
     * <b>DOM</b>: Implements {@link SVGAnimatedNumber#getAnimVal()}.
     */
    public float getAnimVal() {
        if (hasAnimVal) {
            return animVal;
        }
        if (!valid) {
            update();
        }
        return baseVal;
    }

    /**
     * Sets the animated value.
     */
    public void setAnimatedValue(float animVal) {
        hasAnimVal = true;
        this.animVal = animVal;
        fireAnimatedAttributeListeners();
    }

    /**
     * Removes the animated value.
     */
    public void resetAnimatedValue() {
        hasAnimVal = false;
        fireAnimatedAttributeListeners();
    }

    /**
     * Called when an Attr node has been added.
     */
    public void attrAdded(Attr node, String newv) {
        if (!changing) {
            valid = false;
        }
        fireBaseAttributeListeners();
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }

    /**
     * Called when an Attr node has been modified.
     */
    public void attrModified(Attr node, String oldv, String newv) {
        if (!changing) {
            valid = false;
        }
        fireBaseAttributeListeners();
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }

    /**
     * Called when an Attr node has been removed.
     */
    public void attrRemoved(Attr node, String oldv) {
        if (!changing) {
            valid = false;
        }
        fireBaseAttributeListeners();
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }
}
