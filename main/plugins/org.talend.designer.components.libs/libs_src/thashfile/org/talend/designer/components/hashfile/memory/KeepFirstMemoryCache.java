// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.components.hashfile.memory;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.talend.designer.components.hashfile.common.Cache;

/**
 * Memory {@link Cache}, which keeps only first record with specific key in case of key duplication 
 */
public class KeepFirstMemoryCache<V> implements Cache<V> {

    /**
     * Stores first met records
     */
    private Map<V, V> firstMetCache;
    
    /**
     * Constructor creates synchronized map to store records, which key was firstly met
     */
    public KeepFirstMemoryCache() {
        firstMetCache = Collections.synchronizedMap(new LinkedHashMap<V, V>());
    }
    
    /**
     * {@inheritDoc}
     * 
     * Iterator must be manually synched by user 
     */
    @Override
    public Iterator<V> iterator() {
        return new NotRemovingIterator<V>(firstMetCache.keySet().iterator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V put(V value) {
        if (value != null) {
            if (!firstMetCache.containsKey(value)) {
                firstMetCache.put(value, value);
                return value;
            }
        }
        return null;
    }

}