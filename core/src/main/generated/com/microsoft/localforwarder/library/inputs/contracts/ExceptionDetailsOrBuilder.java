// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ExceptionDetails.proto

package com.microsoft.localforwarder.library.inputs.contracts;

public interface ExceptionDetailsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:contracts.ExceptionDetails)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional int32 id = 1;</code>
   */
  int getId();

  /**
   * <code>optional int32 outerId = 2;</code>
   */
  int getOuterId();

  /**
   * <code>optional string typeName = 3;</code>
   */
  java.lang.String getTypeName();
  /**
   * <code>optional string typeName = 3;</code>
   */
  com.google.protobuf.ByteString
      getTypeNameBytes();

  /**
   * <code>optional string message = 4;</code>
   */
  java.lang.String getMessage();
  /**
   * <code>optional string message = 4;</code>
   */
  com.google.protobuf.ByteString
      getMessageBytes();

  /**
   * <pre>
   * the default is true
   * </pre>
   *
   * <code>optional .google.protobuf.BoolValue hasFullStack = 5;</code>
   */
  boolean hasHasFullStack();
  /**
   * <pre>
   * the default is true
   * </pre>
   *
   * <code>optional .google.protobuf.BoolValue hasFullStack = 5;</code>
   */
  com.google.protobuf.BoolValue getHasFullStack();
  /**
   * <pre>
   * the default is true
   * </pre>
   *
   * <code>optional .google.protobuf.BoolValue hasFullStack = 5;</code>
   */
  com.google.protobuf.BoolValueOrBuilder getHasFullStackOrBuilder();

  /**
   * <code>optional string stack = 6;</code>
   */
  java.lang.String getStack();
  /**
   * <code>optional string stack = 6;</code>
   */
  com.google.protobuf.ByteString
      getStackBytes();

  /**
   * <code>repeated .contracts.StackFrame parsedStack = 7;</code>
   */
  java.util.List<com.microsoft.localforwarder.library.inputs.contracts.StackFrame> 
      getParsedStackList();
  /**
   * <code>repeated .contracts.StackFrame parsedStack = 7;</code>
   */
  com.microsoft.localforwarder.library.inputs.contracts.StackFrame getParsedStack(int index);
  /**
   * <code>repeated .contracts.StackFrame parsedStack = 7;</code>
   */
  int getParsedStackCount();
  /**
   * <code>repeated .contracts.StackFrame parsedStack = 7;</code>
   */
  java.util.List<? extends com.microsoft.localforwarder.library.inputs.contracts.StackFrameOrBuilder> 
      getParsedStackOrBuilderList();
  /**
   * <code>repeated .contracts.StackFrame parsedStack = 7;</code>
   */
  com.microsoft.localforwarder.library.inputs.contracts.StackFrameOrBuilder getParsedStackOrBuilder(
      int index);
}