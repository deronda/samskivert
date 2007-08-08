//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
// 
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.servlet.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import com.samskivert.util.ArrayIntSet;
import com.samskivert.util.StringUtil;

/**
 * Utility functions for fetching and manipulating request parameters (form fields).
 */
public class ParameterUtil
{
    /** A default date that can be placed in fields to communicate the appropriate format. Is
     * treated the same as the empty string by {@link #getDateParameter}. */
    public static final String DATE_TEMPLATE = "YYYY-MM-DD";

    /**
     * An interface for validating form parameters.
     */
    public static interface ParameterValidator
    {
        public void validateParameter (String name, String value)
            throws DataValidationException;
    }

    /**
     * Fetches the supplied parameter from the request. If the parameter does not exist, either
     * null or the empty string will be returned depending on the value of the
     * <code>returnNull</code> parameter.
     */
    public static String getParameter (HttpServletRequest req, String name, boolean returnNull)
    {
	String value = req.getParameter(name);
        if (value == null) {
            return returnNull ? null : "";
        } else {
            return value.trim();
        }
    }

    /**
     * Fetches the supplied parameter from the request and converts it to a float. If the parameter
     * does not exist or is not a well-formed float, a data validation exception is thrown with the
     * supplied message.
     */
    public static float requireFloatParameter (
        HttpServletRequest req, String name, String invalidDataMessage)
	throws DataValidationException
    {
	return parseFloatParameter(getParameter(req, name, false), invalidDataMessage);
    }

    /**
     * Fetches the supplied parameter from the request and converts it to an integer. If the
     * parameter does not exist or is not a well-formed integer, a data validation exception is
     * thrown with the supplied message.
     */
    public static int requireIntParameter (
        HttpServletRequest req, String name, String invalidDataMessage)
	throws DataValidationException
    {
	return parseIntParameter(getParameter(req, name, false), invalidDataMessage);
    }

    /**
     * Fetches the supplied parameter from the request and converts it to an integer. If the
     * parameter does not exist or is not a well-formed integer, a data validation exception is
     * thrown with the supplied message.
     */
    public static int requireIntParameter (HttpServletRequest req, String name,
                                           String invalidDataMessage, ParameterValidator validator)
	throws DataValidationException
    {
        String value = getParameter(req, name, false);
        validator.validateParameter(name, value);
	return parseIntParameter(value, invalidDataMessage);
    }

    /**
     * Fetches the supplied parameter from the request and converts it to an integer. If the
     * parameter does not exist or is not a well-formed integer, a data validation exception is
     * thrown with the supplied message.
     */
    public static int requireIntParameter (HttpServletRequest req, String name, int low, int high,
                                           String invalidDataMessage)
	throws DataValidationException
    {
        return requireIntParameter(
            req, name, invalidDataMessage, new IntRangeValidator(low, high, invalidDataMessage));
    }

    /**
     * Fetches all the values from the request with the specified name and converts them to an
     * IntSet.  If the parameter does not exist or is not a well-formed integer, a data validation
     * exception is thrown with the supplied message.
     */
    public static ArrayIntSet getIntParameters (
        HttpServletRequest req, String name, String invalidDataMessage)
        throws DataValidationException
    {
        ArrayIntSet ints = new ArrayIntSet();
        String[] values = req.getParameterValues(name);
        if (values != null) {
            for (int ii = 0; ii < values.length; ii++) {
                if (!StringUtil.isBlank(values[ii])) {
                    ints.add(parseIntParameter(values[ii], invalidDataMessage));
                }
            }
        }
        return ints;
    }

    /**
     * Fetches all the values from the request with the specified name and converts them to a
     * HashSet.
     */
    public static HashSet<String> getParameters (HttpServletRequest req, String name)
        throws DataValidationException
    {
        HashSet<String> set = new HashSet<String>();
        String[] values = req.getParameterValues(name);
        if (values != null) {
            for (int ii = 0; ii < values.length; ii++) {
                if (!StringUtil.isBlank(values[ii])) {
                    set.add(values[ii]);
                }
            }
        }
        return set;
    }

    /**
     * Fetches the supplied parameter from the request. If the parameter does not exist,
     * <code>defval</code> is returned.
     */
    public static String getParameter (HttpServletRequest req, String name, String defval)
    {
	String value = req.getParameter(name);
        return StringUtil.isBlank(value) ? defval : value.trim();
    }

    /**
     * Fetches the supplied parameter from the request and converts it to an integer. If the
     * parameter does not exist, <code>defval</code> is returned. If the parameter is not a
     * well-formed integer, a data validation exception is thrown with the supplied message.
     */
    public static int getIntParameter (
        HttpServletRequest req, String name, int defval, String invalidDataMessage)
	throws DataValidationException
    {
	String value = getParameter(req, name, false);
        if (StringUtil.isBlank(value)) {
            return defval;
        }
        return parseIntParameter(value, invalidDataMessage);
    }

    /**
     * Fetches the supplied parameter from the request and converts it to a long. If the parameter
     * does not exist, <code>defval</code> is returned. If the parameter is not a well-formed
     * integer, a data validation exception is thrown with the supplied message.
     */
    public static long getLongParameter (
        HttpServletRequest req, String name, long defval, String invalidDataMessage)
	throws DataValidationException
    {
	String value = getParameter(req, name, false);
        if (StringUtil.isBlank(value)) {
            return defval;
        }
        return parseLongParameter(value, invalidDataMessage);
    }

    /**
     * Fetches the supplied parameter from the request. If the parameter does not exist, a data
     * validation exception is thrown with the supplied message.
     */
    public static String requireParameter (
        HttpServletRequest req, String name, String missingDataMessage)
	throws DataValidationException
    {
	String value = getParameter(req, name, true);
	if (StringUtil.isBlank(value)) {
	    throw new DataValidationException(missingDataMessage);
	}
	return value;
    }

    /**
     * Fetches the supplied parameter from the request and ensures that it is no longer than
     * maxLength.
     *
     * Note that use of this method could be dangerous. If the specified HttpServletRequest is used
     * to pre-fill in values on a form, it will not know to use the truncated version. A user may
     * enter a version that is too long, your code will see a truncated version, but then the user
     * will see on the page again the full-length reproduction of what they typed in. Be careful.
     */
    public static String requireParameter (
        HttpServletRequest req, String name, String missingDataMessage, int maxLength)
	throws DataValidationException
    {
        return StringUtil.truncate(
            requireParameter(req, name, missingDataMessage), maxLength);
    }

    /**
     * Fetches the supplied parameter from the request and converts it to a date. The value of the
     * parameter should be a date formatted like so: 2001-12-25. If the parameter does not exist or
     * is not a well-formed date, a data validation exception is thrown with the supplied message.
     */
    public static Date requireDateParameter (
        HttpServletRequest req, String name, String invalidDataMessage)
	throws DataValidationException
    {
        return parseDateParameter(getParameter(req, name, false), invalidDataMessage);
    }

    /**
     * Similar to {@link #requireDateParameter} but returns a SQL date.
     */
    public static java.sql.Date requireSQLDateParameter (
        HttpServletRequest req, String name, String invalidDataMessage)
	throws DataValidationException
    {
        return new java.sql.Date(requireDateParameter(req, name, invalidDataMessage).getTime());
    }

    /**
     * Fetches the supplied parameter from the request and converts it to a date. The value of the
     * parameter should be a date formatted like so: 2001-12-25. If the parameter does not exist,
     * null is returned. If the parameter is not a well-formed date, a data validation exception is
     * thrown with the supplied message.
     */
    public static Date getDateParameter (
        HttpServletRequest req, String name, String invalidDataMessage)
	throws DataValidationException
    {
	String value = getParameter(req, name, false);
        if (StringUtil.isBlank(value) ||
            DATE_TEMPLATE.equalsIgnoreCase(value)) {
            return null;
        }
        return parseDateParameter(value, invalidDataMessage);
    }

    /**
     * Similar to {@link #getDateParameter} but returns a SQL date.
     */
    public static java.sql.Date getSQLDateParameter (
        HttpServletRequest req, String name, String invalidDataMessage)
	throws DataValidationException
    {
        Date when = getDateParameter(req, name, invalidDataMessage);
        return (when == null) ? null : new java.sql.Date(when.getTime());
    }

    /**
     * Returns true if the specified parameter is set in the request.
     *
     * @return true if the specified parameter is set in the request context, false otherwise.
     */
    public static boolean isSet (HttpServletRequest req, String name)
    {
        return isSet(req, name, false);
    }

    /**
     * Returns true if the specified parameter is set to a non-blank value in the request, false if
     * the value is blank, or <code>defaultValue</code> if the parameter is unspecified.
     *
     * @return true if the specified parameter is set to a non-blank value in the request, false if
     * the value is blank, or <code>defaultValue</code> if the parameter is unspecified.
     */
    public static boolean isSet (HttpServletRequest req, String name, boolean defaultValue)
    {
        String value = getParameter(req, name, true);
        return (value == null) ? defaultValue : !StringUtil.isBlank(req.getParameter(name));
    }

    /**
     * Returns true if the specified parameter is equal to the supplied value. If the parameter is
     * not set in the request, false is returned.
     *
     * @param name The parameter whose value should be compared with the supplied value.
     * @param value The value to which the parameter may be equal. This should not be null.
     *
     * @return true if the specified parameter is equal to the supplied parameter, false otherwise.
     */
    public static boolean parameterEquals (HttpServletRequest req, String name, String value)
    {
	return value.equals(getParameter(req, name, false));
    }

    /**
     * Internal method to parse integer values.
     */
    protected static int parseIntParameter (String value, String invalidDataMessage)
        throws DataValidationException
    {
        try {
	    return Integer.parseInt(value);
	} catch (NumberFormatException nfe) {
	    throw new DataValidationException(invalidDataMessage);
	}
    }

    /**
     * Internal method to parse long values.
     */
    protected static long parseLongParameter (String value, String invalidDataMessage)
        throws DataValidationException
    {
        try {
	    return Long.parseLong(value);
	} catch (NumberFormatException nfe) {
	    throw new DataValidationException(invalidDataMessage);
	}
    }

    /**
     * Internal method to parse a float value.
     */
    protected static float parseFloatParameter (String value, String invalidDataMessage)
        throws DataValidationException
    {
        try {
	    return Float.parseFloat(value);
	} catch (NumberFormatException nfe) {
	    throw new DataValidationException(invalidDataMessage);
	}
    }

    /**
     * Internal method to parse a date.
     */
    protected static Date parseDateParameter (String value, String invalidDataMessage)
	throws DataValidationException
    {
	try {
            return _dparser.parse(value);
	} catch (ParseException pe) {
	    throw new DataValidationException(invalidDataMessage);
	}
    }

    /** Makes sure integers are within a range. */
    protected static class IntRangeValidator implements ParameterValidator
    {
        public IntRangeValidator (int low, int high, String outOfRangeError) {
            _low = low;
            _high = high;
            _err = outOfRangeError;
        }

        public void validateParameter (String name, String value) throws DataValidationException {
            try {
                int ivalue = Integer.parseInt(value);
                if ((ivalue >= _low) && (ivalue <= _high)) {
                    return;
                }
            } catch (Exception e) {
                // fall through
            }
            throw new DataValidationException(_err);
        }

        protected int _low, _high;
        protected String _err;
    }

    /** We use this to parse dates in requireDateParameter(). */
    protected static SimpleDateFormat _dparser = new SimpleDateFormat("yyyy-MM-dd");
}
