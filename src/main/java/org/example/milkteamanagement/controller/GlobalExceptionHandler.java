package org.example.milkteamanagement.controller;

// Duplicate Spring advice was causing bean name conflicts with
// org.example.milkteamanagement.exception.GlobalExceptionHandler.
// Keep this file as a non-bean helper placeholder to preserve the source file,
// but prevent component scanning from registering a second advice bean.
class PosApiExceptionSupport {
}


