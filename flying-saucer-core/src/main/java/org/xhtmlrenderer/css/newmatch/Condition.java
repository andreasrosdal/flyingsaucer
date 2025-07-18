/*
 * Condition.java
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
package org.xhtmlrenderer.css.newmatch;

import org.w3c.dom.Node;
import org.xhtmlrenderer.css.extend.AttributeResolver;
import org.xhtmlrenderer.css.extend.TreeResolver;
import org.xhtmlrenderer.css.parser.CSSParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Locale.ROOT;


/**
 * Part of a Selector
 *
 * @author tstgm
 */
abstract class Condition {

    abstract boolean matches(Node e, AttributeResolver attRes, TreeResolver treeRes);

    /**
     * the CSS condition [attribute]
     */
    static Condition createAttributeExistsCondition(String namespaceURI, String name) {
        return new AttributeExistsCondition(namespaceURI, name);
    }

    /**
     * the CSS condition [attribute^=value]
     */
    static Condition createAttributePrefixCondition(String namespaceURI, String name, String value) {
        return new AttributePrefixCondition(namespaceURI, name, value);
    }

    /**
     * the CSS condition [attribute$=value]
     */
    static Condition createAttributeSuffixCondition(String namespaceURI, String name, String value) {
        return new AttributeSuffixCondition(namespaceURI, name, value);
    }

    /**
     * the CSS condition [attribute*=value]
     */
    static Condition createAttributeSubstringCondition(String namespaceURI, String name, String value) {
        return new AttributeSubstringCondition(namespaceURI, name, value);
    }

    /**
     * the CSS condition [attribute=value]
     */
    static Condition createAttributeEqualsCondition(String namespaceURI, String name, String value) {
        return new AttributeEqualsCondition(namespaceURI, name, value);
    }

    /**
     * the CSS condition [attribute~=value]
     */
    static Condition createAttributeMatchesListCondition(String namespaceURI, String name, String value) {
        return new AttributeMatchesListCondition(namespaceURI, name, value);
    }

    /**
     * the CSS condition [attribute|=value]
     */
    static Condition createAttributeMatchesFirstPartCondition(String namespaceURI, String name, String value) {
        return new AttributeMatchesFirstPartCondition(namespaceURI, name, value);
    }

    /**
     * the CSS condition .class
     */
    static Condition createClassCondition(String className) {
        return new ClassCondition(className);
    }

    /**
     * the CSS condition #ID
     */
    static Condition createIDCondition(String id) {
        return new IDCondition(id);
    }

    /**
     * the CSS condition lang(Xx)
     */
    static Condition createLangCondition(String lang) {
        return new LangCondition(lang);
    }

    /**
     * the CSS condition that element has pseudo-class :first-child
     */
    static Condition createFirstChildCondition() {
        return new FirstChildCondition();
    }

    /**
     * the CSS condition that element has pseudo-class :last-child
     */
    static Condition createLastChildCondition() {
        return new LastChildCondition();
    }

    /**
     * the CSS condition that element has pseudo-class :nth-child(an+b)
     */
    static Condition createNthChildCondition(String number) {
        return NthChildCondition.fromString(number);
    }

    /**
     * the CSS condition that element has pseudo-class :even
     */
    static Condition createEvenChildCondition() {
        return new EvenChildCondition();
    }

    /**
     * the CSS condition that element has pseudo-class :odd
     */
    static Condition createOddChildCondition() {
        return new OddChildCondition();
    }

    /**
     * the CSS condition that element has pseudo-class :link
     */
    static Condition createLinkCondition() {
        return new LinkCondition();
    }

    /**
     * for unsupported or invalid CSS
     */
    static Condition createUnsupportedCondition() {
        return new UnsupportedCondition();
    }

    private abstract static class AttributeCompareCondition extends Condition {
        private final String _namespaceURI;
        private final String _name;
        private final String _value;

        protected abstract boolean compare(String attrValue, String conditionValue);

        AttributeCompareCondition(String namespaceURI, String name, String value) {
            _namespaceURI = namespaceURI;
            _name = name;
            _value = value;
        }

        @Override
        boolean matches(Node e, AttributeResolver attRes, TreeResolver treeRes) {
            if (attRes == null) {
                return false;
            }
            String val = attRes.getAttributeValue(e, _namespaceURI, _name);
            if (val == null) {
                return false;
            }

            return compare(val, _value);
        }
    }

    private static class AttributeExistsCondition extends AttributeCompareCondition {
        private AttributeExistsCondition(String namespaceURI, String name) {
            super(namespaceURI, name, null);
        }

        @Override
        protected boolean compare(String attrValue, String conditionValue) {
            return !attrValue.isEmpty();
        }
    }

    private static class AttributeEqualsCondition extends AttributeCompareCondition {
        private AttributeEqualsCondition(String namespaceURI, String name, String value) {
            super(namespaceURI, name, value);
        }

        @Override
        protected boolean compare(String attrValue, String conditionValue) {
            return attrValue.equals(conditionValue);
        }
    }

    private static class AttributePrefixCondition extends AttributeCompareCondition {
        private AttributePrefixCondition(String namespaceURI, String name, String value) {
            super(namespaceURI, name, value);
        }

        @Override
        protected boolean compare(String attrValue, String conditionValue) {
            return attrValue.startsWith(conditionValue);
        }
    }

    private static class AttributeSuffixCondition extends AttributeCompareCondition {
        private AttributeSuffixCondition(String namespaceURI, String name, String value) {
            super(namespaceURI, name, value);
        }

        @Override
        protected boolean compare(String attrValue, String conditionValue) {
            return attrValue.endsWith(conditionValue);
        }
    }

    private static class AttributeSubstringCondition extends AttributeCompareCondition {
        private AttributeSubstringCondition(String namespaceURI, String name, String value) {
            super(namespaceURI, name, value);
        }

        @Override
        protected boolean compare(String attrValue, String conditionValue) {
            return attrValue.contains(conditionValue);
        }
    }

    private static class AttributeMatchesListCondition extends AttributeCompareCondition {
        private AttributeMatchesListCondition(String namespaceURI, String name, String value) {
            super(namespaceURI, name, value);
        }

        @Override
        protected boolean compare(String attrValue, String conditionValue) {
            String[] ca = split(attrValue, ' ');
            boolean matched = false;
            for (String s : ca) {
                if (conditionValue.equals(s)) {
                    matched = true;
                    break;
                }
            }
            return matched;
        }
    }

    private static class AttributeMatchesFirstPartCondition extends AttributeCompareCondition {
        private AttributeMatchesFirstPartCondition(String namespaceURI, String name, String value) {
            super(namespaceURI, name, value);
        }

        @Override
        protected boolean compare(String attrValue, String conditionValue) {
            String[] ca = split(attrValue, '-');
            return conditionValue.equals(ca[0]);
        }
    }

    final static class ClassCondition extends Condition {
        private final String className;
        private final int classNameLength;

        private ClassCondition(String className) {
            this.className = className;
            this.classNameLength = className.length();
        }

        @Override
        boolean matches(Node e, AttributeResolver attRes, TreeResolver treeRes) {
            if (attRes == null) {
                return false;
            }
            String c = attRes.getClass(e);
            return c != null && containsClassName(c);
        }

        boolean containsClassName(String classAttribute) {
            return containsClassName(classAttribute, -1);
        }

        private boolean containsClassName(String classAttribute, int fromIndex) {
            // This is much faster than calling `split()` and comparing individual values in a loop.
            // NOTE: In jQuery, for example, the attribute value first has whitespace normalized to spaces. But
            // in an XML DOM, space normalization in attributes is supposed to have happened already.
            int index = classAttribute.indexOf(className, fromIndex);
            if (index == -1) return false;

            return isWhitespace(classAttribute, index - 1)
                    && isWhitespace(classAttribute, index + classNameLength)
                    || containsClassName(classAttribute, index + classNameLength);
        }

        private boolean isWhitespace(String classAttribute, int index) {
            return index < 0 || index >= classAttribute.length() || Character.isWhitespace(classAttribute.charAt(index));
        }
    }

    private static class IDCondition extends Condition {
        private final String _id;

        private IDCondition(String id) {
            _id = id;
        }

        @Override
        boolean matches(Node e, AttributeResolver attRes, TreeResolver treeRes) {
            if (attRes == null) {
                return false;
            }
            return _id.equals(attRes.getID(e));
        }
    }

    static class LangCondition extends Condition {
        private final String _lang;

        private LangCondition(String lang) {
            _lang = lang;
        }

        @Override
        boolean matches(Node e, AttributeResolver attRes, TreeResolver treeRes) {
            if (attRes == null) {
                return false;
            }
            String langAttribute = attRes.getLang(e);
            return langAttribute != null && matches(langAttribute);
        }

        boolean matches(String langAttribute) {
            if (_lang.equalsIgnoreCase(langAttribute)) {
                return true;
            }
            int i = langAttribute.indexOf('-');
            return i == _lang.length() && langAttribute.substring(0, i).equalsIgnoreCase(_lang);
        }

    }

    private static class FirstChildCondition extends Condition {
        @Override
        boolean matches(Node e, AttributeResolver attRes, TreeResolver treeRes) {
            return treeRes.isFirstChildElement(e);
        }
    }

    private static class LastChildCondition extends Condition {
        @Override
        boolean matches(Node e, AttributeResolver attRes, TreeResolver treeRes) {
            return treeRes.isLastChildElement(e);
        }
    }

    /**
     * {@code <An+B>} from <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/:nth-child">nth-child</a>
     * Represents elements whose numeric position in a series of siblings matches the pattern An+B,
     * for every non-negative integer n. The index of the first element is 1.
     * The values A and B must both be integers.
     */
    static class NthChildCondition extends Condition {
        private static final Pattern pattern = Pattern.compile("([-+]?)(\\d*)n(\\s*([-+])\\s*(\\d+))?");

        private final int a;
        private final int b;

        NthChildCondition(int a, int b) {
            this.a = a;
            this.b = b;
        }

        @Override
        boolean matches(Node e, AttributeResolver attRes, TreeResolver treeRes) {
            // getPositionOfElement() starts at 0, CSS spec starts at 1
            int position = treeRes.getPositionOfElement(e) + 1;
            return matches(position);
        }

        boolean matches(int position) {
            return switch (a) {
                case 0 -> position == b;
                default -> {
                    int an = position - b;
                    yield an % a == 0 &&
                        (an == 0 || an > 0 == a > 0); // effectively same as "an / a >= 0"
                }
            };
        }

        static NthChildCondition fromString(String number) {
            number = number.trim().toLowerCase(ROOT);

            return switch (number) {
                case "even" -> new NthChildCondition(2, 0);
                case "odd" -> new NthChildCondition(2, 1);
                default -> {
                    try {
                        yield new NthChildCondition(0, Integer.parseInt(number));
                    } catch (NumberFormatException e) {
                        Matcher m = pattern.matcher(number);

                        if (!m.matches()) {
                            throw new CSSParseException("Invalid nth-child selector: " + number, -1, e);
                        } else {
                            int a = m.group(2).isEmpty() ? 1 : Integer.parseInt(m.group(2));
                            int b = (m.group(5) == null) ? 0 : Integer.parseInt(m.group(5));
                            if ("-".equals(m.group(1))) {
                                a *= -1;
                            }
                            if ("-".equals(m.group(4))) {
                                b *= -1;
                            }

                            yield new NthChildCondition(a, b);
                        }
                    }
                }
            };
        }
    }

    private static class EvenChildCondition extends Condition {
        @Override
        boolean matches(Node e, AttributeResolver attRes, TreeResolver treeRes) {
            int position = treeRes.getPositionOfElement(e);
            return position >= 0 && position % 2 == 0;
        }
    }

    private static class OddChildCondition extends Condition {
        @Override
        boolean matches(Node e, AttributeResolver attRes, TreeResolver treeRes) {
            int position = treeRes.getPositionOfElement(e);
            return position % 2 == 1;
        }
    }

    private static class LinkCondition extends Condition {
        @Override
        boolean matches(Node e, AttributeResolver attRes, TreeResolver treeRes) {
            return attRes.isLink(e);
        }

    }

    /**
     * represents unsupported (or invalid) css, never matches
     */
    private static class UnsupportedCondition extends Condition {
        @Override
        boolean matches(Node e, AttributeResolver attRes, TreeResolver treeRes) {
            return false;
        }

    }

    private static String[] split(String s, char ch) {
        if (s.indexOf(ch) == -1) {
            return new String[] { s };
        } else {
            List<String> result = new ArrayList<>();

            int last = 0;
            int next;

            while ((next = s.indexOf(ch, last)) != -1) {
                if (next != last) {
                    result.add(s.substring(last, next));
                }
                last = next + 1;
            }

            if (last != s.length()) {
                result.add(s.substring(last));
            }

            return result.toArray(new String[0]);
        }
    }
}

