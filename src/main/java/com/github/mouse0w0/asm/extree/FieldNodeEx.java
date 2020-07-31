package com.github.mouse0w0.asm.extree;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.TypeAnnotationNode;
import org.objectweb.asm.tree.UnsupportedClassVersionException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FieldNodeEx extends FieldVisitor {

    /**
     * The field's access flags (see {@link org.objectweb.asm.Opcodes}). This field also indicates if
     * the field is synthetic and/or deprecated.
     */
    public int access;

    /**
     * The field's name.
     */
    public String name;

    /**
     * The field's descriptor (see {@link org.objectweb.asm.Type}).
     */
    public String desc;

    /**
     * The field's signature. May be {@literal null}.
     */
    public String signature;

    /**
     * The field's initial value. This field, which may be {@literal null} if the field does not have
     * an initial value, must be an {@link Integer}, a {@link Float}, a {@link Long}, a {@link Double}
     * or a {@link String}.
     */
    public Object value;

    public Map<String, AnnotationNodeEx> annotations;

    /**
     * The runtime visible type annotations of this field. May be {@literal null}.
     */
    public List<TypeAnnotationNode> visibleTypeAnnotations;

    /**
     * The runtime invisible type annotations of this field. May be {@literal null}.
     */
    public List<TypeAnnotationNode> invisibleTypeAnnotations;

    /**
     * The non standard attributes of this field. * May be {@literal null}.
     */
    public List<Attribute> attrs;

    /**
     * Constructs a new {@link FieldNode}. <i>Subclasses must not use this constructor</i>. Instead,
     * they must use the {@link #FieldNodeEx(int, int, String, String, String, Object)} version.
     *
     * @param access     the field's access flags (see {@link org.objectweb.asm.Opcodes}). This parameter
     *                   also indicates if the field is synthetic and/or deprecated.
     * @param name       the field's name.
     * @param descriptor the field's descriptor (see {@link org.objectweb.asm.Type}).
     * @param signature  the field's signature.
     * @param value      the field's initial value. This parameter, which may be {@literal null} if the
     *                   field does not have an initial value, must be an {@link Integer}, a {@link Float}, a {@link
     *                   Long}, a {@link Double} or a {@link String}.
     * @throws IllegalStateException If a subclass calls this constructor.
     */
    public FieldNodeEx(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final Object value) {
        this(/* latest api = */ Opcodes.ASM8, access, name, descriptor, signature, value);
    }

    /**
     * Constructs a new {@link FieldNode}.
     *
     * @param api        the ASM API version implemented by this visitor. Must be one of {@link
     *                   Opcodes#ASM4}, {@link Opcodes#ASM5}, {@link Opcodes#ASM6}, {@link Opcodes#ASM7} or {@link
     *                   Opcodes#ASM8}.
     * @param access     the field's access flags (see {@link org.objectweb.asm.Opcodes}). This parameter
     *                   also indicates if the field is synthetic and/or deprecated.
     * @param name       the field's name.
     * @param descriptor the field's descriptor (see {@link org.objectweb.asm.Type}).
     * @param signature  the field's signature.
     * @param value      the field's initial value. This parameter, which may be {@literal null} if the
     *                   field does not have an initial value, must be an {@link Integer}, a {@link Float}, a {@link
     *                   Long}, a {@link Double} or a {@link String}.
     */
    public FieldNodeEx(
            final int api,
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final Object value) {
        super(api);
        this.access = access;
        this.name = name;
        this.desc = descriptor;
        this.signature = signature;
        this.value = value;
    }

    public AnnotationNodeEx getAnnotation(String descriptor) {
        return annotations == null ? null : annotations.get(descriptor);
    }

    // -----------------------------------------------------------------------------------------------
    // Implementation of the FieldVisitor abstract class
    // -----------------------------------------------------------------------------------------------

    @Override
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
        AnnotationNodeEx annotation = new AnnotationNodeEx(descriptor, visible);
        if (annotations == null) {
            annotations = new LinkedHashMap<>(2);
        }
        annotations.put(descriptor, annotation);
        return annotation;
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(
            final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
        TypeAnnotationNode typeAnnotation = new TypeAnnotationNode(typeRef, typePath, descriptor);
        if (visible) {
            visibleTypeAnnotations = Util.add(visibleTypeAnnotations, typeAnnotation);
        } else {
            invisibleTypeAnnotations = Util.add(invisibleTypeAnnotations, typeAnnotation);
        }
        return typeAnnotation;
    }

    @Override
    public void visitAttribute(final Attribute attribute) {
        attrs = Util.add(attrs, attribute);
    }

    @Override
    public void visitEnd() {
        // Nothing to do.
    }

    // -----------------------------------------------------------------------------------------------
    // Accept methods
    // -----------------------------------------------------------------------------------------------

    /**
     * Checks that this field node is compatible with the given ASM API version. This method checks
     * that this node, and all its children recursively, do not contain elements that were introduced
     * in more recent versions of the ASM API than the given version.
     *
     * @param api an ASM API version. Must be one of {@link Opcodes#ASM4}, {@link Opcodes#ASM5},
     *            {@link Opcodes#ASM6} or {@link Opcodes#ASM7}.
     */
    public void check(final int api) {
        if (api == Opcodes.ASM4) {
            if (visibleTypeAnnotations != null && !visibleTypeAnnotations.isEmpty()) {
                throw new UnsupportedClassVersionException();
            }
            if (invisibleTypeAnnotations != null && !invisibleTypeAnnotations.isEmpty()) {
                throw new UnsupportedClassVersionException();
            }
        }
    }

    /**
     * Makes the given class visitor visit this field.
     *
     * @param classVisitor a class visitor.
     */
    public void accept(final ClassVisitor classVisitor) {
        FieldVisitor fieldVisitor = classVisitor.visitField(access, name, desc, signature, value);
        if (fieldVisitor == null) {
            return;
        }
        // Visit the annotations.
        if (annotations != null) {
            for (AnnotationNodeEx annotation : annotations.values()) {
                annotation.accept(fieldVisitor.visitAnnotation(annotation.desc, annotation.visible));
            }
        }
        if (visibleTypeAnnotations != null) {
            for (int i = 0, n = visibleTypeAnnotations.size(); i < n; ++i) {
                TypeAnnotationNode typeAnnotation = visibleTypeAnnotations.get(i);
                typeAnnotation.accept(
                        fieldVisitor.visitTypeAnnotation(
                                typeAnnotation.typeRef, typeAnnotation.typePath, typeAnnotation.desc, true));
            }
        }
        if (invisibleTypeAnnotations != null) {
            for (int i = 0, n = invisibleTypeAnnotations.size(); i < n; ++i) {
                TypeAnnotationNode typeAnnotation = invisibleTypeAnnotations.get(i);
                typeAnnotation.accept(
                        fieldVisitor.visitTypeAnnotation(
                                typeAnnotation.typeRef, typeAnnotation.typePath, typeAnnotation.desc, false));
            }
        }
        // Visit the non standard attributes.
        if (attrs != null) {
            for (int i = 0, n = attrs.size(); i < n; ++i) {
                fieldVisitor.visitAttribute(attrs.get(i));
            }
        }
        fieldVisitor.visitEnd();
    }
}
