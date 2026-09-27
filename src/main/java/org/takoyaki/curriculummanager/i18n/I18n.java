package org.takoyaki.curriculummanager.i18n;

import org.takoyaki.curriculummanager.model.Terminology;
import org.takoyaki.curriculummanager.service.TerminologyService;
import org.takoyaki.curriculummanager.util.TerminologyUtils;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;

public final class I18n {
    private static final ResourceBundle SOURCE_BUNDLE = JsonResourceBundle.load(
            "/org/takoyaki/curriculummanager/messages_ja.json"
    );
    private static volatile Terminology cachedTerminology;

    private I18n() {
    }

    public static ResourceBundle bundle() {
        ResourceBundle source = sourceBundle();
        return new TerminologyResourceBundle(source, terminology());
    }

    public static String text(String key, Object... arguments) {
        String pattern = bundle().getString(key);
        return arguments.length == 0 ? pattern : MessageFormat.format(pattern, arguments);
    }

    public static String raw(String key, Object... arguments) {
        String pattern = sourceBundle().getString(key);
        return arguments.length == 0 ? pattern : MessageFormat.format(pattern, arguments);
    }

    public static void reloadTerminology() {
        cachedTerminology = null;
    }

    private static Terminology terminology() {
        Terminology terminology = cachedTerminology;

        if (terminology == null) {
            synchronized (I18n.class) {
                terminology = cachedTerminology;

                if (terminology == null) {
                    terminology = new TerminologyService().getTerminology();
                    cachedTerminology = terminology;
                }
            }
        }

        return terminology;
    }

    private static ResourceBundle sourceBundle() {
        return SOURCE_BUNDLE;
    }

    private static final class TerminologyResourceBundle extends ResourceBundle {
        private final ResourceBundle source;
        private final Terminology terminology;

        private TerminologyResourceBundle(ResourceBundle source, Terminology terminology) {
            this.source = source;
            this.terminology = terminology;
        }

        @Override
        protected Object handleGetObject(String key) {
            return TerminologyUtils.format(source.getString(key), terminology);
        }

        @Override
        public Enumeration<String> getKeys() {
            return Collections.enumeration(source.keySet());
        }
    }
}
