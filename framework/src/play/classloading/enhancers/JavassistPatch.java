package play.classloading.enhancers;

import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;

public class JavassistPatch {
    
    private static Object toAnnoType_(Annotation anno, ClassPool cp)
            throws ClassNotFoundException {
        try {
            ClassLoader cl = cp.getClassLoader();
            return anno.toAnnotationType(cl, cp);
        } catch (ClassNotFoundException e) {
            ClassLoader cl2 = cp.getClass().getClassLoader();
            return anno.toAnnotationType(cl2, cp);
        }
    }

    private static Object getAnnotationType_(Class<?> clz, ClassPool cp,
            AnnotationsAttribute a1, AnnotationsAttribute a2)
            throws ClassNotFoundException {
        Annotation[] anno1, anno2;

        if (a1 == null)
            anno1 = null;
        else
            anno1 = a1.getAnnotations();

        if (a2 == null)
            anno2 = null;
        else
            anno2 = a2.getAnnotations();

        String typeName = clz.getName();
        if (anno1 != null)
            for (int i = 0; i < anno1.length; i++)
                if (anno1[i].getTypeName().equals(typeName))
                    return toAnnoType_(anno1[i], cp);

        if (anno2 != null)
            for (int i = 0; i < anno2.length; i++)
                if (anno2[i].getTypeName().equals(typeName))
                    return toAnnoType_(anno2[i], cp);

        return null;
    }

    public static Object getAnnotation(CtBehavior ctBehavior, Class<?> annType) throws ClassNotFoundException {
        MethodInfo mi = ctBehavior.getMethodInfo2();
        AnnotationsAttribute ainfo = (AnnotationsAttribute)
                    mi.getAttribute(AnnotationsAttribute.invisibleTag);  
        AnnotationsAttribute ainfo2 = (AnnotationsAttribute)
                    mi.getAttribute(AnnotationsAttribute.visibleTag);  
        return getAnnotationType_(annType, ctBehavior.getDeclaringClass().getClassPool(), ainfo, ainfo2);
    }

}
