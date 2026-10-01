package com.email.validator.smart_email_validator.utils;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public final class DnsLookup {

    private DnsLookup() {
    }

    public static List<String> lookup(String domain, String recordType)
            throws Exception {

        Hashtable<String, String> environment = new Hashtable<>();
        environment.put(
                Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.dns.DnsContextFactory"
        );
        environment.put("com.sun.jndi.dns.timeout.initial", "3000");
        environment.put("com.sun.jndi.dns.timeout.retries", "1");

        DirContext context = new InitialDirContext(environment);

        try {
            Attributes attributes = context.getAttributes(
                    domain,
                    new String[]{recordType}
            );

            Attribute attribute = attributes.get(recordType);

            if (attribute == null) {
                return List.of();
            }

            List<String> records = new ArrayList<>();

            NamingEnumeration<?> values = attribute.getAll();

            while (values.hasMore()) {
                records.add(String.valueOf(values.next()));
            }

            return records;
        } finally {
            context.close();
        }
    }
}