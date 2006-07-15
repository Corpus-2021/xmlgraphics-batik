/*

   Copyright 2006  The Apache Software Foundation 

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
package org.apache.batik.bridge;

import java.awt.Color;
import java.awt.Paint;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.batik.anim.AnimationEngine;
import org.apache.batik.anim.AnimationException;
import org.apache.batik.anim.AnimationTarget;
import org.apache.batik.anim.SMILConstants;
import org.apache.batik.anim.timing.TimedDocumentRoot;
import org.apache.batik.anim.timing.TimedElement;
import org.apache.batik.anim.values.AnimatableAngleValue;
import org.apache.batik.anim.values.AnimatableAngleOrIdentValue;
import org.apache.batik.anim.values.AnimatableBooleanValue;
import org.apache.batik.anim.values.AnimatableIntegerValue;
import org.apache.batik.anim.values.AnimatableLengthValue;
import org.apache.batik.anim.values.AnimatableLengthListValue;
import org.apache.batik.anim.values.AnimatableLengthOrIdentValue;
import org.apache.batik.anim.values.AnimatableNumberValue;
import org.apache.batik.anim.values.AnimatableNumberListValue;
import org.apache.batik.anim.values.AnimatableNumberOrPercentageValue;
import org.apache.batik.anim.values.AnimatablePathDataValue;
import org.apache.batik.anim.values.AnimatablePointListValue;
import org.apache.batik.anim.values.AnimatablePreserveAspectRatioValue;
import org.apache.batik.anim.values.AnimatableNumberOrIdentValue;
import org.apache.batik.anim.values.AnimatableStringValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.anim.values.AnimatableColorValue;
import org.apache.batik.anim.values.AnimatablePaintValue;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.StringValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.dom.svg.SVGStylableElement;
import org.apache.batik.parser.DefaultPreserveAspectRatioHandler;
import org.apache.batik.parser.FloatArrayProducer;
import org.apache.batik.parser.DefaultLengthHandler;
import org.apache.batik.parser.LengthArrayProducer;
import org.apache.batik.parser.LengthHandler;
import org.apache.batik.parser.LengthListParser;
import org.apache.batik.parser.LengthParser;
import org.apache.batik.parser.NumberListParser;
import org.apache.batik.parser.PathArrayProducer;
import org.apache.batik.parser.PathParser;
import org.apache.batik.parser.PointsParser;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PreserveAspectRatioParser;
import org.apache.batik.util.RunnableQueue;
import org.apache.batik.util.XMLConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGAngle;
import org.w3c.dom.svg.SVGLength;
import org.w3c.dom.svg.SVGPreserveAspectRatio;

/**
 * An AnimationEngine for SVG documents.
 *
 * @author <a href="mailto:cam%40mcc%2eid%2eau">Cameron McCormack</a>
 * @version $Id$
 */
public class SVGAnimationEngine extends AnimationEngine {

    /**
     * The BridgeContext to use for value parsing.
     */
    protected BridgeContext ctx;

    /**
     * The CSSEngine used for CSS value parsing.
     */
    protected CSSEngine cssEngine;

    /**
     * Whether animation processing has started.  This affects whether
     * animation element bridges add their animation on to the initial
     * bridge list, or process them immediately.
     */
    protected boolean started;

    /**
     * The factory for unparsed string values.
     */
    protected UncomputedAnimatableStringValueFactory 
        uncomputedAnimatableStringValueFactory = 
            new UncomputedAnimatableStringValueFactory();
    
    /**
     * The factory for length-or-ident values.
     */
    protected AnimatableLengthOrIdentFactory
        animatableLengthOrIdentFactory = new AnimatableLengthOrIdentFactory();

    /**
     * The factory for number-or-ident values.
     */
    protected AnimatableNumberOrIdentFactory
        animatableNumberOrIdentFactory = new AnimatableNumberOrIdentFactory();

    /**
     * Factories for {@link AnimatableValue} parsing.
     */
    protected Factory[] factories = {
        null, // TYPE_UNKNOWN
        new AnimatableIntegerValueFactory(), // TYPE_INTEGER
        new AnimatableNumberValueFactory(), // TYPE_NUMBER
        new AnimatableLengthValueFactory(), // TYPE_LENGTH
        null, // TYPE_NUMBER_OPTIONAL_NUMBER
        new AnimatableAngleValueFactory(), // TYPE_ANGLE
        new AnimatableColorValueFactory(), // TYPE_COLOR
        new AnimatablePaintValueFactory(), // TYPE_PAINT
        null, // TYPE_PERCENTAGE
        null, // TYPE_TRANSFORM_LIST
        uncomputedAnimatableStringValueFactory, // TYPE_URI
        null, // TYPE_FREQUENCY
        null, // TYPE_TIME
        new AnimatableNumberListValueFactory(), // TYPE_NUMBER_LIST
        new AnimatableLengthListValueFactory(), // TYPE_LENGTH_LIST
        uncomputedAnimatableStringValueFactory, // TYPE_IDENT
        uncomputedAnimatableStringValueFactory, // TYPE_CDATA
        animatableLengthOrIdentFactory, // TYPE_LENGTH_OR_INHERIT
        uncomputedAnimatableStringValueFactory, // TYPE_IDENT_LIST
        uncomputedAnimatableStringValueFactory, // TYPE_CLIP_VALUE
        uncomputedAnimatableStringValueFactory, // TYPE_URI_OR_IDENT
        uncomputedAnimatableStringValueFactory, // TYPE_CURSOR_VALUE
        new AnimatablePathDataFactory(), // TYPE_PATH_DATA
        uncomputedAnimatableStringValueFactory, // TYPE_ENABLE_BACKGROUND_VALUE
        null, // TYPE_TIME_VALUE_LIST
        animatableNumberOrIdentFactory, // TYPE_NUMBER_OR_INHERIT
        uncomputedAnimatableStringValueFactory, // TYPE_FONT_FAMILY_VALUE
        null, // TYPE_FONT_FACE_FONT_SIZE_VALUE
        animatableNumberOrIdentFactory, // TYPE_FONT_WEIGHT_VALUE
        new AnimatableAngleOrIdentFactory(), // TYPE_ANGLE_OR_IDENT
        null, // TYPE_KEY_SPLINES_VALUE
        new AnimatablePointListValueFactory(), // TYPE_POINTS_VALUE
        new AnimatablePreserveAspectRatioValueFactory(), // TYPE_PRESERVE_ASPECT_RATIO_VALUE
        null, // TYPE_URI_LIST
        uncomputedAnimatableStringValueFactory, // TYPE_LENGTH_LIST_OR_IDENT
        null, // TYPE_CHARACTER_OR_UNICODE_RANGE_LIST
        null, // TYPE_UNICODE_RANGE_LIST
        null, // TYPE_FONT_VALUE
        null, // TYPE_FONT_DECSRIPTOR_SRC_VALUE
        animatableLengthOrIdentFactory, // TYPE_FONT_SIZE_VALUE
        animatableLengthOrIdentFactory, // TYPE_BASELINE_SHIFT_VALUE
        animatableLengthOrIdentFactory, // TYPE_KERNING_VALUE
        animatableLengthOrIdentFactory, // TYPE_SPACING_VALUE
        animatableLengthOrIdentFactory, // TYPE_LINE_HEIGHT_VALUE
        animatableNumberOrIdentFactory, // TYPE_FONT_SIZE_ADJUST_VALUE
        null, // TYPE_LANG_VALUE
        null, // TYPE_LANG_LIST_VALUE
        new AnimatableNumberOrPercentageValueFactory(), // TYPE_NUMBER_OR_PERCENTAGE
        null, // TYPE_TIMING_SPECIFIER_LIST
        new AnimatableBooleanValueFactory(), // TYPE_BOOLEAN
    };

    /**
     * Whether the document is an SVG 1.2 document.
     */
    protected boolean isSVG12;

    /**
     * List of bridges that will be initialized when the document is started.
     */
    protected LinkedList initialBridges = new LinkedList();

    /**
     * Event listener for the document 'load' event.
     */
    protected EventListener loadEventListener = new LoadListener();

    /**
     * A StyleMap used by the {@link Factory}s when computing CSS values.
     */
    protected StyleMap dummyStyleMap;

    /**
     * The thread that ticks the animation engine.
     */
    protected AnimationThread animationThread;
    
    /**
     * Set of SMIL animation event names for SVG 1.1.
     */
    protected static HashSet animationEventNames11 = new HashSet();

    /**
     * Set of SMIL animation event names for SVG 1.2.
     */
    protected static HashSet animationEventNames12 = new HashSet();

    static {
        String[] eventNamesCommon = {
            "click", "mousedown", "mouseup", "mouseover", "mousemove",
            "mouseout", "beginEvent", "endEvent"
        };
        String[] eventNamesSVG11 = {
            "DOMSubtreeModified", "DOMNodeInserted", "DOMNodeRemoved",
            "DOMNodeRemovedFromDocument", "DOMNodeInsertedIntoDocument",
            "DOMAttrModified", "DOMCharacterDataModified", "SVGLoad",
            "SVGUnload", "SVGAbort", "SVGError", "SVGResize", "SVGScroll",
            "repeatEvent"
        };
        String[] eventNamesSVG12 = {
            "load", "resize", "scroll", "zoom"
        };
        for (int i = 0; i < eventNamesCommon.length; i++) {
            animationEventNames11.add(eventNamesCommon[i]);
            animationEventNames12.add(eventNamesCommon[i]);
        }
        for (int i = 0; i < eventNamesSVG11.length; i++) {
            animationEventNames11.add(eventNamesSVG11[i]);
        }
        for (int i = 0; i < eventNamesSVG12.length; i++) {
            animationEventNames12.add(eventNamesSVG12[i]);
        }
    }

    /**
     * Creates a new SVGAnimationEngine.
     */
    public SVGAnimationEngine(Document doc, BridgeContext ctx) {
        super(doc);
        this.ctx = ctx;
        SVGOMDocument d = (SVGOMDocument) doc;
        cssEngine = d.getCSSEngine();
        dummyStyleMap = new StyleMap(cssEngine.getNumberOfProperties());
        isSVG12 = d.isSVG12();

        SVGOMElement svg = (SVGOMElement) d.getDocumentElement();
        svg.addEventListener("SVGLoad", loadEventListener, false);
    }

    /**
     * Disposes this animation engine.
     */
    public void dispose() {
        SVGOMElement svg = (SVGOMElement) document.getDocumentElement();
        svg.removeEventListener("SVGLoad", loadEventListener, false);
    }

    /**
     * Adds an animation element bridge to the list of bridges that
     * require initializing when the document is started.
     */
    public void addInitialBridge(SVGAnimationElementBridge b) {
        if (initialBridges != null) {
            initialBridges.add(b);
        }
    }

    /**
     * Returns whether animation processing has begun.
     */
    public boolean hasStarted() {
        return started;
    }

    /**
     * Parses an AnimatableValue.
     */
    public AnimatableValue parseAnimatableValue(Element animElt,
                                                AnimationTarget target,
                                                String ns, String ln,
                                                boolean isCSS,
                                                String s) {
        SVGOMElement elt = (SVGOMElement) target.getElement();
        int type;
        if (isCSS) {
            type = elt.getPropertyType(ln);
        } else {
            type = elt.getAttributeType(ns, ln);
        }
        Factory factory = factories[type];
        if (factory == null) {
            String an = ns == null ? ln : '{' + ns + '}' + ln;
            throw new BridgeException
                (ctx, animElt, "attribute.not.animatable",
                 new Object[] { target.getElement().getNodeName(), an });
        }
        return factories[type].createValue(target, ns, ln, isCSS, s);
    }

    /**
     * Returns an AnimatableValue for the underlying value of a CSS property.
     */
    public AnimatableValue getUnderlyingCSSValue(Element animElt,
                                                 AnimationTarget target,
                                                 String pn) {
        ValueManager vms[] = cssEngine.getValueManagers();
        int idx = cssEngine.getPropertyIndex(pn);
        if (idx != -1) {
            int type = vms[idx].getPropertyType();
            Factory factory = factories[type];
            if (factory == null) {
                throw new BridgeException
                    (ctx, animElt, "attribute.not.animatable",
                     new Object[] { target.getElement().getNodeName(), pn });
            }
            SVGStylableElement e = (SVGStylableElement) target.getElement();
            CSSStyleDeclaration over = e.getOverrideStyle();
            String oldValue = over.getPropertyValue(pn);
            if (oldValue != null) {
                over.removeProperty(pn);
            }
            Value v = cssEngine.getComputedStyle(e, null, idx);
            if (oldValue != null) {
                over.setProperty(pn, oldValue, null);
            }
            return factories[type].createValue(target, pn, v);
        }
        // XXX Doesn't handle shorthands.
        return null;
    }

    /**
     * Creates a new returns a new TimedDocumentRoot object for the document.
     */
    protected TimedDocumentRoot createDocumentRoot() {
        return new AnimationRoot();
    }

    /**
     * A class for the root time container.
     */
    protected class AnimationRoot extends TimedDocumentRoot {

        /**
         * Creates a new AnimationRoot object.
         */
        public AnimationRoot() {
            super(!isSVG12, isSVG12);
        }

        /**
         * Returns the namespace URI of the event that corresponds to the given
         * animation event name.
         */
        protected String getEventNamespaceURI(String eventName) {
            if (!isSVG12) {
                return null;
            }
            if (eventName.equals("focusin")
                    || eventName.equals("focusout")
                    || eventName.equals("activate")
                    || animationEventNames12.contains(eventName)) {
                return XMLConstants.XML_EVENTS_NAMESPACE_URI;
            }
            return null;
        }

        /**
         * Returns the type of the event that corresponds to the given
         * animation event name.
         */
        protected String getEventType(String eventName) {
            if (eventName.equals("focusin")) {
                return "DOMFocusIn";
            } else if (eventName.equals("focusout")) {
                return "DOMFocusOut";
            } else if (eventName.equals("activate")) {
                return "DOMActivate";
            }
            if (isSVG12) {
                if (animationEventNames12.contains(eventName)) {
                    return eventName;
                }
            } else {
                if (animationEventNames11.contains(eventName)) {
                    return eventName;
                }
            }
            return null;
        }

        /**
         * Returns the name of the repeat event.
         * @return "repeatEvent" for SVG
         */
        protected String getRepeatEventName() {
            return SMILConstants.SMIL_REPEAT_EVENT_NAME;
        }

        /**
         * Fires a TimeEvent of the given type on this element.
         * @param eventType the type of TimeEvent ("beginEvent", "endEvent"
         *                  or "repeatEvent"/"repeat").
         * @param time the timestamp of the event object
         */
        protected void fireTimeEvent(String eventType, Calendar time,
                                     int detail) {
            AnimationSupport.fireTimeEvent
                ((EventTarget) document, eventType, time, detail);
        }

        /**
         * Invoked to indicate this timed element became active at the
         * specified time.
         * @param begin the time the element became active, in document simple time
         */
        protected void toActive(float begin) {
        }

        /**
         * Invoked to indicate that this timed element became inactive.
         * @param isFrozen whether the element is frozen or not
         */
        protected void toInactive(boolean isFrozen) {
        }

        /**
         * Invoked to indicate that this timed element has had its fill removed.
         */
        protected void removeFill() {
        }

        /**
         * Invoked to indicate that this timed element has been sampled at the
         * given time.
         * @param simpleTime the sample time in local simple time
         * @param simpleDur the simple duration of the element
         * @param repeatIteration the repeat iteration during which the element
         *                        was sampled
         */
        protected void sampledAt(float simpleTime, float simpleDur,
                                 int repeatIteration) {
        }

        /**
         * Invoked to indicate that this timed element has been sampled
         * at the end of its active time, at an integer multiple of the
         * simple duration.  This is the "last" value that will be used
         * for filling, which cannot be sampled normally.
         */
        protected void sampledLastValue(int repeatIteration) {
        }

        /**
         * Returns the timed element with the given ID.
         */
        protected TimedElement getTimedElementById(String id) {
            return AnimationSupport.getTimedElementById(id, document);
        }

        /**
         * Returns the event target with the given ID.
         */
        protected EventTarget getEventTargetById(String id) {
            return AnimationSupport.getEventTargetById(id, document);
        }

        /**
         * Returns the event target that is the parent of the given
         * timed element.  Used for eventbase timing specifiers where
         * the element ID is omitted.
         */
        protected EventTarget getParentEventTarget(TimedElement e) {
            return AnimationSupport.getParentEventTarget(e);
        }

        /**
         * Returns the event target that should be listened to for
         * access key events.
         */
        protected EventTarget getRootEventTarget() {
            return (EventTarget) document;
        }

        /**
         * Returns the DOM element that corresponds to this timed element, if
         * such a DOM element exists.
         */
        public Element getElement() {
            return null;
        }

        /**
         * Returns whether this timed element comes before the given timed
         * element in document order.
         */
        public boolean isBefore(TimedElement other) {
            return false;
        }
    }

    /**
     * Listener class for the document 'load' event.
     */
    protected class LoadListener implements EventListener {

        /**
         * Handles the event.
         */
        public void handleEvent(Event evt) {
            if (evt.getTarget() != evt.getCurrentTarget()) {
                return;
            }
            try {
                try {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(evt.getTimeStamp());
                    timedDocumentRoot.resetDocument(cal);
                    Object[] bridges = initialBridges.toArray();
                    initialBridges = null;
                    for (int i = 0; i < bridges.length; i++) {
                        SVGAnimationElementBridge bridge =
                            (SVGAnimationElementBridge) bridges[i];
                        bridge.initializeAnimation();
                    }
                    for (int i = 0; i < bridges.length; i++) {
                        SVGAnimationElementBridge bridge =
                            (SVGAnimationElementBridge) bridges[i];
                        bridge.initializeTimedElement();
                    }
                    started = true;
                    tick(0);
                    // animationThread = new AnimationThread();
                    // animationThread.start();
                    ctx.getUpdateManager().getUpdateRunnableQueue().setIdleRunnable
                        (new AnimationTickRunnable());
                } catch (AnimationException ex) {
                    throw new BridgeException(ctx, ex.getElement().getElement(),
                                              ex.getMessage());
                }
            } catch (BridgeException ex) {
                if (ctx.getUserAgent() == null) {
                    ex.printStackTrace();
                } else {
                    ctx.getUserAgent().displayError(ex);
                }
            }
        }
    }

    /**
     * Idle runnable to tick the animation.
     */
    protected class AnimationTickRunnable implements Runnable {
        protected Calendar time = Calendar.getInstance();
        double second = -1.;
        int idx = -1;
        int frames;
        public void run() {
            try {
                try {
                    time.setTimeInMillis(System.currentTimeMillis());
                    float t = timedDocumentRoot.convertWallclockTime(time);
                    if (Math.floor(t) > second) {
                        second = Math.floor(t);
                        System.err.println("fps: " + frames);
                        frames = 0;
                    }
                    tick(t);
                    frames++;
                } catch (AnimationException ex) {
                    throw new BridgeException(ctx, ex.getElement().getElement(),
                                              ex.getMessage());
                }
            } catch (BridgeException ex) {
                if (ctx.getUserAgent() == null) {
                    ex.printStackTrace();
                } else {
                    ctx.getUserAgent().displayError(ex);
                }
            }
            //Thread.yield();
            try {
                Thread.sleep(1);
            } catch (InterruptedException ie) {
            }
        }
    }

    /**
     * The thread that ticks the animation.
     */
    protected class AnimationThread extends Thread {
        
        /**
         * The current time.
         */
        protected Calendar time = Calendar.getInstance();
        
        /**
         * The RunnableQueue to perform the animation in.
         */
        protected RunnableQueue runnableQueue =
            ctx.getUpdateManager().getUpdateRunnableQueue();
        
        /**
         * The animation ticker Runnable.
         */
        protected Ticker ticker = new Ticker();

        /**
         * Ticks the animation over as fast as possible.
         */
        public void run() {
            if (true) {
                for (;;) {
                    time.setTimeInMillis(System.currentTimeMillis());
                    ticker.t = timedDocumentRoot.convertWallclockTime(time);
                    try {
                        runnableQueue.invokeAndWait(ticker);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            } else {
                ticker.t = 1;
                while (ticker.t < 10) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                    }
                    try {
                        runnableQueue.invokeAndWait(ticker);
                    } catch (InterruptedException e) {
                        return;
                    }
                    ticker.t++;
                }
            }
        }
        
        /**
         * A runnable that ticks the animation engine.
         */
        protected class Ticker implements Runnable {
            
            /**
             * The document time to tick at next.
             */
            protected float t;
            
            /**
             * Ticks the animation over.
             */
            public void run() {
                tick(t);
            }
        }
    }

    // AnimatableValue factories

    /**
     * Interface for AnimatableValue factories.
     */
    protected interface Factory {

        /**
         * Creates a new AnimatableValue from a string.
         */
        AnimatableValue createValue(AnimationTarget target, String ns,
                                    String ln, boolean isCSS, String s);

        /**
         * Creates a new AnimatableValue from a CSS {@link Value}.
         */
        AnimatableValue createValue(AnimationTarget target, String pn, Value v);
    }

    /**
     * Factory class for AnimatableValues for CSS properties.
     * XXX Shorthand properties are not supported.
     */
    protected abstract class CSSValueFactory implements Factory {

        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            // XXX Always parsing as a CSS value.
            return createValue(target, ln, createCSSValue(target, ln, s));
        }

        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            CSSStylableElement elt = (CSSStylableElement) target.getElement();
            v = computeValue(elt, pn, v);
            return createAnimatableValue(target, pn, v);
        }

        /**
         * Creates a new AnimatableValue from a CSS {@link Value}, after
         * computation and inheritance.
         */
        protected abstract AnimatableValue createAnimatableValue
            (AnimationTarget target, String pn, Value v);

        /**
         * Creates a new CSS {@link Value} from a string.
         */
        protected Value createCSSValue(AnimationTarget t, String pn, String s) {
            CSSStylableElement elt = (CSSStylableElement) t.getElement();
            Value v = cssEngine.parsePropertyValue(elt, pn, s);
            return computeValue(elt, pn, v);
        }

        /**
         * Computes a CSS {@link Value} and performance inheritance if the
         * specified value is 'inherit'.
         */
        protected Value computeValue(CSSStylableElement elt, String pn,
                                     Value v) {
            ValueManager[] vms = cssEngine.getValueManagers();
            int idx = cssEngine.getPropertyIndex(pn);
            if (idx != -1) {
                if (v.getCssValueType() == CSSValue.CSS_INHERIT) {
                    elt = CSSEngine.getParentCSSStylableElement(elt);
                    if (elt != null) {
                        return cssEngine.getComputedStyle(elt, null, idx);
                    }
                    return vms[idx].getDefaultValue();
                }
                v = vms[idx].computeValue(elt, null, cssEngine, idx,
                                          dummyStyleMap, v);
            }
            return v;
        }
    }

    /**
     * Factory class for {@link AnimatableBooleanValue}s.
     */
    protected class AnimatableBooleanValueFactory implements Factory {

        /**
         * Creates a new AnimatableValue from a string.
         */
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            return new AnimatableBooleanValue(target, "true".equals(s));
        }

        /**
         * Creates a new AnimatableValue from a CSS {@link Value}.
         */
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            return new AnimatableBooleanValue(target,
                                              "true".equals(v.getCssText()));
        }
    }

    /**
     * Factory class for {@link AnimatableIntegerValue}s.
     */
    protected class AnimatableIntegerValueFactory implements Factory {

        /**
         * Creates a new AnimatableValue from a string.
         */
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            return new AnimatableIntegerValue(target, Integer.parseInt(s));
        }

        /**
         * Creates a new AnimatableValue from a CSS {@link Value}.
         */
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            return new AnimatableIntegerValue(target,
                                              Math.round(v.getFloatValue()));
        }
    }

    /**
     * Factory class for {@link AnimatableNumberValue}s.
     */
    protected class AnimatableNumberValueFactory implements Factory {

        /**
         * Creates a new AnimatableValue from a string.
         */
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            return new AnimatableNumberValue(target, Float.parseFloat(s));
        }

        /**
         * Creates a new AnimatableValue from a CSS {@link Value}.
         */
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            return new AnimatableNumberValue(target, v.getFloatValue());
        }
    }

    /**
     * Factory class for {@link AnimatableNumberOrPercentageValue}s.
     */
    protected class AnimatableNumberOrPercentageValueFactory
            implements Factory {

        /**
         * Creates a new AnimatableValue from a string.
         */
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            float v;
            boolean pc;
            if (s.charAt(s.length() - 1) == '%') {
                v = Float.parseFloat(s.substring(0, s.length() - 1));
                pc = true;
            } else {
                v = Float.parseFloat(s);
                pc = false;
            }
            return new AnimatableNumberOrPercentageValue(target, v, pc);
        }

        /**
         * Creates a new AnimatableValue from a CSS {@link Value}.
         */
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            switch (v.getPrimitiveType()) {
                case CSSPrimitiveValue.CSS_PERCENTAGE:
                    return new AnimatableNumberOrPercentageValue
                        (target, v.getFloatValue(), true);
                case CSSPrimitiveValue.CSS_NUMBER:
                    return new AnimatableNumberOrPercentageValue
                        (target, v.getFloatValue());
            }
            // XXX Do something better than returning null.
            return null;
        }
    }

    /**
     * Factory class for {@link AnimatablePreserveAspectRatioValue}s.
     */
    protected class AnimatablePreserveAspectRatioValueFactory implements Factory {

        /**
         * The parsed 'align' value.
         */
        protected short align;

        /**
         * The parsed 'meetOrSlice' value.
         */
        protected short meetOrSlice;

        /**
         * Parser for preserveAspectRatio values.
         */
        protected PreserveAspectRatioParser parser =
            new PreserveAspectRatioParser();

        /**
         * Handler for the preserveAspectRatio parser.
         */
        protected DefaultPreserveAspectRatioHandler handler =
            new DefaultPreserveAspectRatioHandler() {

            /**
             * Implements {@link
             * PreserveAspectRatioHandler#startPreserveAspectRatio()}.
             */
            public void startPreserveAspectRatio() throws ParseException {
                align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_UNKNOWN;
                meetOrSlice = SVGPreserveAspectRatio.SVG_MEETORSLICE_UNKNOWN;
            }
            
            /**
             * Implements {@link PreserveAspectRatioHandler#none()}.
             */
            public void none() throws ParseException {
                align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_NONE;
            }

            /**
             * Implements {@link PreserveAspectRatioHandler#xMaxYMax()}.
             */
            public void xMaxYMax() throws ParseException {
                align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMAXYMAX;
            }

            /**
             * Implements {@link PreserveAspectRatioHandler#xMaxYMid()}.
             */
            public void xMaxYMid() throws ParseException {
                align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMAXYMID;
            }

            /**
             * Implements {@link PreserveAspectRatioHandler#xMaxYMin()}.
             */
            public void xMaxYMin() throws ParseException {
                align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMAXYMIN;
            }

            /**
             * Implements {@link PreserveAspectRatioHandler#xMidYMax()}.
             */
            public void xMidYMax() throws ParseException {
                align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMIDYMAX;
            }

            /**
             * Implements {@link PreserveAspectRatioHandler#xMidYMid()}.
             */
            public void xMidYMid() throws ParseException {
                align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMIDYMID;
            }

            /**
             * Implements {@link PreserveAspectRatioHandler#xMidYMin()}.
             */
            public void xMidYMin() throws ParseException {
                align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMIDYMIN;
            }

            /**
             * Implements {@link PreserveAspectRatioHandler#xMinYMax()}.
             */
            public void xMinYMax() throws ParseException {
                align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMINYMAX;
            }

            /**
             * Implements {@link PreserveAspectRatioHandler#xMinYMid()}.
             */
            public void xMinYMid() throws ParseException {
                align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMINYMID;
            }

            /**
             * Implements {@link PreserveAspectRatioHandler#xMinYMin()}.
             */
            public void xMinYMin() throws ParseException {
                align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMINYMIN;
            }

            /**
             * Implements {@link PreserveAspectRatioHandler#meet()}.
             */
            public void meet() throws ParseException {
                meetOrSlice = SVGPreserveAspectRatio.SVG_MEETORSLICE_MEET;
            }

            /**
             * Implements {@link PreserveAspectRatioHandler#slice()}.
             */
            public void slice() throws ParseException {
                meetOrSlice = SVGPreserveAspectRatio.SVG_MEETORSLICE_SLICE;
            }
        };

        /**
         * Creates a new AnimatablePreserveAspectRatioValueFactory.
         */
        public AnimatablePreserveAspectRatioValueFactory() {
            parser.setPreserveAspectRatioHandler(handler);
        }

        /**
         * Creates a new AnimatableValue from a string.
         */
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            try {
                parser.parse(s);
                return new AnimatablePreserveAspectRatioValue(target, align,
                                                              meetOrSlice);
            } catch (ParseException e) {
                // XXX Do something better than returning null.
                return null;
            }
        }

        /**
         * Creates a new AnimatableValue from a CSS {@link Value}.  Returns null
         * since preserveAspectRatio values aren't used in CSS values.
         */
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            return null;
        }
    }

    /**
     * Factory class for {@link AnimatableLengthValue}s.
     */
    protected class AnimatableLengthValueFactory implements Factory {

        /**
         * The parsed length unit type.
         */
        protected short type;

        /**
         * The parsed length value.
         */
        protected float value;

        /**
         * Parser for lengths.
         */
        protected LengthParser parser = new LengthParser();

        /**
         * Handler for the length parser.
         */
        protected LengthHandler handler = new DefaultLengthHandler() {
            public void startLength() throws ParseException {
                type = SVGLength.SVG_LENGTHTYPE_NUMBER;
            }
            public void lengthValue(float v) throws ParseException {
                value = v;
            }
            public void em() throws ParseException {
                type = SVGLength.SVG_LENGTHTYPE_EMS;
            }
            public void ex() throws ParseException {
                type = SVGLength.SVG_LENGTHTYPE_EXS;
            }
            public void in() throws ParseException {
                type = SVGLength.SVG_LENGTHTYPE_IN;
            }
            public void cm() throws ParseException {
                type = SVGLength.SVG_LENGTHTYPE_CM;
            }
            public void mm() throws ParseException {
                type = SVGLength.SVG_LENGTHTYPE_MM;
            }
            public void pc() throws ParseException {
                type = SVGLength.SVG_LENGTHTYPE_PC;
            }
            public void pt() throws ParseException {
                type = SVGLength.SVG_LENGTHTYPE_PT;
            }
            public void px() throws ParseException {
                type = SVGLength.SVG_LENGTHTYPE_PX;
            }
            public void percentage() throws ParseException {
                type = SVGLength.SVG_LENGTHTYPE_PERCENTAGE;
            }
            public void endLength() throws ParseException {
            }
        };

        /**
         * Creates a new AnimatableLengthValueFactory.
         */
        public AnimatableLengthValueFactory() {
            parser.setLengthHandler(handler);
        }

        /**
         * Creates a new AnimatableValue from a string.
         */
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            short pcInterp = target.getPercentageInterpretation(ns, ln, isCSS);
            try {
                parser.parse(s);
                return new AnimatableLengthValue
                    (target, type, value, pcInterp);
            } catch (ParseException e) {
                // XXX Do something better than returning null.
                return null;
            }
        }

        /**
         * Creates a new AnimatableValue from a CSS {@link Value}.
         */
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            return new AnimatableIntegerValue(target,
                                              Math.round(v.getFloatValue()));
        }
    }

    /**
     * Factory class for {@link AnimatableLengthListValue}s.
     */
    protected class AnimatableLengthListValueFactory implements Factory {

        /**
         * Parser for length lists.
         */
        protected LengthListParser parser = new LengthListParser();

        /**
         * The producer class that accumulates the lengths.
         */
        protected LengthArrayProducer producer = new LengthArrayProducer();

        /**
         * Creates a new AnimatableLengthListValueFactory.
         */
        public AnimatableLengthListValueFactory() {
            parser.setLengthListHandler(producer);
        }

        /**
         * Creates a new AnimatableValue from a string.
         */
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            try {
                short pcInterp = target.getPercentageInterpretation
                    (ns, ln, isCSS);
                parser.parse(s);
                return new AnimatableLengthListValue
                    (target, producer.getLengthTypeArray(),
                     producer.getLengthValueArray(),
                     pcInterp);
            } catch (ParseException e) {
                // XXX Do something better than returning null.
                return null;
            }
        }

        /**
         * Creates a new AnimatableValue from a CSS {@link Value}.  Returns null
         * since point lists aren't used in CSS values.
         */
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            return null;
        }
    }

    /**
     * Factory class for {@link AnimationNumberListValue}s.
     */
    protected class AnimatableNumberListValueFactory implements Factory {

        /**
         * Parser for number lists.
         */
        protected NumberListParser parser = new NumberListParser();

        /**
         * The producer class that accumulates the numbers.
         */
        protected FloatArrayProducer producer = new FloatArrayProducer();

        /**
         * Creates a new AnimatableNumberListValueFactory.
         */
        public AnimatableNumberListValueFactory() {
            parser.setNumberListHandler(producer);
        }

        /**
         * Creates a new AnimatableValue from a string.
         */
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            try {
                parser.parse(s);
                return new AnimatableNumberListValue(target,
                                                     producer.getFloatArray());
            } catch (ParseException e) {
                // XXX Do something better than returning null.
                return null;
            }
        }

        /**
         * Creates a new AnimatableValue from a CSS {@link Value}.  Returns null
         * since number lists aren't used in CSS values.
         */
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            return null;
        }
    }

    /**
     * Factory class for {@link AnimationPointListValue}s.
     */
    protected class AnimatablePointListValueFactory implements Factory {

        /**
         * Parser for point lists.
         */
        protected PointsParser parser = new PointsParser();

        /**
         * The producer class that accumulates the points.
         */
        protected FloatArrayProducer producer = new FloatArrayProducer();

        /**
         * Creates a new AnimatablePointListValueFactory.
         */
        public AnimatablePointListValueFactory() {
            parser.setPointsHandler(producer);
        }

        /**
         * Creates a new AnimatableValue from a string.
         */
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            try {
                parser.parse(s);
                return new AnimatablePointListValue(target,
                                                    producer.getFloatArray());
            } catch (ParseException e) {
                // XXX Do something better than returning null.
                return null;
            }
        }

        /**
         * Creates a new AnimatableValue from a CSS {@link Value}.  Returns null
         * since point lists aren't used in CSS values.
         */
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            return null;
        }
    }

    /**
     * Factory class for {@link AnimatablePathDataValue}s.
     */
    protected class AnimatablePathDataFactory implements Factory {

        /**
         * Parser for path data.
         */
        protected PathParser parser = new PathParser();

        /**
         * The producer class that accumulates the path segments.
         */
        protected PathArrayProducer producer = new PathArrayProducer();

        /**
         * Creates a new AnimatablePathDataFactory.
         */
        public AnimatablePathDataFactory() {
            parser.setPathHandler(producer);
        }

        /**
         * Creates a new AnimatableValue from a string.
         */
        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            try {
                parser.parse(s);
                return new AnimatablePathDataValue
                    (target, producer.getPathCommands(),
                     producer.getPathParameters());
            } catch (ParseException e) {
                // XXX Do something better than returning null.
                return null;
            }
        }

        /**
         * Creates a new AnimatableValue from a CSS {@link Value}.  Returns null
         * since point lists aren't used in CSS values.
         */
        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            return null;
        }
    }

    /**
     * Factory class for {@link AnimatableStringValue}s.
     */
    protected class UncomputedAnimatableStringValueFactory implements Factory {

        public AnimatableValue createValue(AnimationTarget target, String ns,
                                           String ln, boolean isCSS, String s) {
            return new AnimatableStringValue(target, s);
        }

        public AnimatableValue createValue(AnimationTarget target, String pn,
                                           Value v) {
            return new AnimatableStringValue(target, v.getCssText());
        }
    }

    /**
     * Factory class for {@link AnimatableLengthOrIdentValue}s.
     */
    protected class AnimatableLengthOrIdentFactory extends CSSValueFactory {

        protected AnimatableValue createAnimatableValue(AnimationTarget target,
                                                        String pn, Value v) {
            if (v instanceof StringValue) {
                return new AnimatableLengthOrIdentValue(target,
                                                        v.getStringValue());
            }
            short pcInterp = target.getPercentageInterpretation(null, pn, true);
            FloatValue fv = (FloatValue) v;
            return new AnimatableLengthOrIdentValue
                (target, fv.getPrimitiveType(), fv.getFloatValue(), pcInterp);
        }
    }

    /**
     * Factory class for {@link AnimatableNumberOrIdentValue}s.
     */
    protected class AnimatableNumberOrIdentFactory extends CSSValueFactory {

        protected AnimatableValue createAnimatableValue(AnimationTarget target,
                                                        String pn, Value v) {
            if (v instanceof StringValue) {
                return new AnimatableNumberOrIdentValue(target,
                                                        v.getStringValue());
            }
            FloatValue fv = (FloatValue) v;
            return new AnimatableNumberOrIdentValue(target, fv.getFloatValue());
        }
    }
    
    /**
     * Factory class for {@link AnimatableAngleValue}s.
     */
    protected class AnimatableAngleValueFactory extends CSSValueFactory {

        protected AnimatableValue createAnimatableValue(AnimationTarget target,
                                                        String pn, Value v) {
            FloatValue fv = (FloatValue) v;
            short unit;
            switch (fv.getPrimitiveType()) {
                case CSSPrimitiveValue.CSS_NUMBER:
                case CSSPrimitiveValue.CSS_DEG:
                    unit = SVGAngle.SVG_ANGLETYPE_DEG;
                    break;
                case CSSPrimitiveValue.CSS_RAD:
                    unit = SVGAngle.SVG_ANGLETYPE_RAD;
                    break;
                case CSSPrimitiveValue.CSS_GRAD:
                    unit = SVGAngle.SVG_ANGLETYPE_GRAD;
                    break;
                default:
                    // XXX Do something better than returning null.
                    return null;
            }
            return new AnimatableAngleValue(target, fv.getFloatValue(), unit);
        }
    }
    
    /**
     * Factory class for {@link AnimatableAngleOrIdentValue}s.
     */
    protected class AnimatableAngleOrIdentFactory extends CSSValueFactory {

        protected AnimatableValue createAnimatableValue(AnimationTarget target,
                                                        String pn, Value v) {
            if (v instanceof StringValue) {
                return new AnimatableAngleOrIdentValue(target,
                                                       v.getStringValue());
            }
            FloatValue fv = (FloatValue) v;
            short unit;
            switch (fv.getPrimitiveType()) {
                case CSSPrimitiveValue.CSS_NUMBER:
                case CSSPrimitiveValue.CSS_DEG:
                    unit = SVGAngle.SVG_ANGLETYPE_DEG;
                    break;
                case CSSPrimitiveValue.CSS_RAD:
                    unit = SVGAngle.SVG_ANGLETYPE_RAD;
                    break;
                case CSSPrimitiveValue.CSS_GRAD:
                    unit = SVGAngle.SVG_ANGLETYPE_GRAD;
                    break;
                default:
                    // XXX Do something better than returning null.
                    return null;
            }
            return new AnimatableAngleOrIdentValue(target, fv.getFloatValue(),
                                                   unit);
        }
    }
    
    /**
     * Factory class for {@link AnimatableColorValue}s.
     */
    protected class AnimatableColorValueFactory extends CSSValueFactory {

        protected AnimatableValue createAnimatableValue(AnimationTarget target,
                                                        String pn, Value v) {
            Paint p = PaintServer.convertPaint
                (target.getElement(), null, v, 1f, ctx);
            if (p instanceof Color) {
                Color c = (Color) p;
                return new AnimatableColorValue(target,
                                                c.getRed() / 255f,
                                                c.getGreen() / 255f,
                                                c.getBlue() / 255f);
            }
            // XXX Indicate that the parsed value wasn't a Color?
            return null;
        }
    }

    /**
     * Factory class for {@link AnimatablePaintValue}s.
     */
    protected class AnimatablePaintValueFactory extends CSSValueFactory {

        /**
         * Creates a new {@link AnimatablePaintValue} from a {@link Color}
         * object.
         */
        protected AnimatablePaintValue createColorPaintValue(AnimationTarget t,
                                                             Color c) {
            return AnimatablePaintValue.createColorPaintValue
                (t, c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f);

        }

        protected AnimatableValue createAnimatableValue(AnimationTarget target,
                                                        String pn, Value v) {
            if (v.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                switch (v.getPrimitiveType()) {
                    case CSSPrimitiveValue.CSS_IDENT:
                        return AnimatablePaintValue.createNonePaintValue(target);
                    case CSSPrimitiveValue.CSS_RGBCOLOR: {
                        Paint p = PaintServer.convertPaint
                            (target.getElement(), null, v, 1f, ctx);
                        return createColorPaintValue(target, (Color) p);
                    }
                    case CSSPrimitiveValue.CSS_URI:
                        return AnimatablePaintValue.createURIPaintValue
                            (target, v.getStringValue());
                }
            } else {
                Value v1 = v.item(0);
                switch (v1.getPrimitiveType()) {
                    case CSSPrimitiveValue.CSS_RGBCOLOR: {
                        Paint p = PaintServer.convertPaint
                            (target.getElement(), null, v, 1f, ctx);
                        return createColorPaintValue(target, (Color) p);
                    }
                    case CSSPrimitiveValue.CSS_URI: {
                        Value v2 = v.item(1);
                        switch (v2.getPrimitiveType()) {
                            case CSSPrimitiveValue.CSS_IDENT:
                                return AnimatablePaintValue.createURINonePaintValue
                                    (target, v1.getStringValue());
                            case CSSPrimitiveValue.CSS_RGBCOLOR: {
                                Paint p = PaintServer.convertPaint
                                    (target.getElement(), null, v.item(1), 1f, ctx);
                                return createColorPaintValue(target, (Color) p);
                            }
                        }
                    }
                }
            }
            // XXX Indicate that the specified Value wasn't a Color?
            return null;
        }
    }
    
    /**
     * Factory class for computed CSS {@link AnimatableStringValue}s.
     */
    protected class AnimatableStringValueFactory extends CSSValueFactory {

        protected AnimatableValue createAnimatableValue(AnimationTarget target,
                                                        String pn, Value v) {
            return new AnimatableStringValue(target, v.getCssText());
        }
    }
}
