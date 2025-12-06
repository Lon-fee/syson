/*******************************************************************************
 * Copyright (c) 2025 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.syson.ai.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.syson.ai.services.ValidationIssue;
import org.springframework.stereotype.Component;

/**
 * Validates SysML V2 code syntax and semantics.
 * Provides comprehensive validation including syntax, naming conventions, and structure.
 *
 * @author AI Services Team
 */
@Component
public class SysMLCodeValidator {
    
    // Standard SysML V2 types
    private static final Set<String> STANDARD_TYPES = Set.of(
        "Real", "Integer", "String", "Boolean",
        "Natural", "Positive", "Rational",
        "Time", "Length", "Mass", "Temperature", "Pressure"
    );
    
    // Valid definition keywords
    private static final Set<String> VALID_DEF_KEYWORDS = Set.of(
        "part", "requirement", "action", "state", "constraint",
        "interface", "allocation", "connection", "attribute",
        "port", "flow", "item", "package", "metadata"
    );
    
    /**
     * Validate SysML V2 code and return list of issues.
     *
     * @param code SysML V2 code to validate
     * @return Validation result with issues
     */
    public ValidationResult validate(String code) {
        if (code == null || code.trim().isEmpty()) {
            return new ValidationResult(false, List.of(
                ValidationIssue.error("Empty code", "Provide SysML V2 code to validate")
            ));
        }
        
        List<ValidationIssue> issues = new ArrayList<>();
        
        // Run all validation checks
        issues.addAll(validateBracesAndSemicolons(code));
        issues.addAll(validateDefinitions(code));
        issues.addAll(validateTypeReferences(code));
        issues.addAll(validateNamingConventions(code));
        issues.addAll(validatePackageStructure(code));
        
        boolean isValid = issues.stream().noneMatch(i -> "ERROR".equals(i.severity()));
        
        return new ValidationResult(isValid, issues);
    }
    
    /**
     * Check for balanced braces and proper semicolon usage.
     */
    private List<ValidationIssue> validateBracesAndSemicolons(String code) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        // Count braces
        long openBraces = code.chars().filter(ch -> ch == '{').count();
        long closeBraces = code.chars().filter(ch -> ch == '}').count();
        
        if (openBraces != closeBraces) {
            issues.add(ValidationIssue.error(
                "Unbalanced braces: " + openBraces + " open, " + closeBraces + " close",
                "Ensure every '{' has a matching '}'"
            ));
        }
        
        // Check for common semicolon issues
        Pattern missingemicolon = Pattern.compile("(\\w+)\\s*:\\s*(\\w+)\\s*$", Pattern.MULTILINE);
        Matcher matcher = missingSemicolon.matcher(code);
        if (matcher.find()) {
            issues.add(ValidationIssue.warning(
                "Possible missing semicolon after type declaration",
                "Add ';' after usage declarations (e.g., 'part engine : Engine;')"
            ));
        }
        
        return issues;
    }
    
    /**
     * Validate definition keywords and syntax.
     */
    private List<ValidationIssue> validateDefinitions(String code) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        // Pattern for definitions: keyword def Name
        Pattern defPattern = Pattern.compile("(\\w+)\\s+def\\s+(\\w+)");
        Matcher matcher = defPattern.matcher(code);
        
        while (matcher.find()) {
            String keyword = matcher.group(1);
            String name = matcher.group(2);
            
            // Check if keyword is valid
            if (!VALID_DEF_KEYWORDS.contains(keyword)) {
                issues.add(ValidationIssue.error(
                    "Invalid definition keyword: '" + keyword + "'",
                    "Use one of: " + VALID_DEF_KEYWORDS
                ));
            }
            
            // Check PascalCase for definitions
            if (!Character.isUpperCase(name.charAt(0))) {
                issues.add(ValidationIssue.warning(
                    "Definition name '" + name + "' should use PascalCase",
                    "Rename to start with uppercase (e.g., 'Engine', 'Vehicle')"
                ));
            }
        }
        
        return issues;
    }
    
    /**
     * Validate type references.
     */
    private List<ValidationIssue> validateTypeReferences(String code) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        // Extract all defined types
        Set<String> definedTypes = extractDefinedTypes(code);
        
        // Pattern for type usage: identifier : Type
        Pattern usagePattern = Pattern.compile("(\\w+)\\s*:\\s*(\\w+)");
        Matcher matcher = usagePattern.matcher(code);
        
        while (matcher.find()) {
            String typeName = matcher.group(2);
            
            // Skip multiplicity markers
            if (typeName.matches("\\d+")) {
                continue;
            }
            
            // Check if type is defined or standard
            if (!definedTypes.contains(typeName) && !STANDARD_TYPES.contains(typeName)) {
                issues.add(ValidationIssue.warning(
                    "Undefined type: '" + typeName + "'",
                    "Define this type or import it from a standard library"
                ));
            }
        }
        
        return issues;
    }
    
    /**
     * Validate naming conventions.
     */
    private List<ValidationIssue> validateNamingConventions(String code) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        // Pattern for usages (not definitions): part identifier : Type
        Pattern usagePattern = Pattern.compile("(\\w+)\\s+(\\w+)\\s*:\\s*(\\w+)(?!\\s+def)");
        Matcher matcher = usagePattern.matcher(code);
        
        while (matcher.find()) {
            String keyword = matcher.group(1);
            String name = matcher.group(2);
            
            // Skip if this is actually a definition
            if (VALID_DEF_KEYWORDS.contains(keyword) && code.substring(matcher.end()).trim().startsWith("def")) {
                continue;
            }
            
            // Check camelCase for usages
            if (Character.isUpperCase(name.charAt(0))) {
                issues.add(ValidationIssue.info(
                    "Usage '" + name + "' should use camelCase (lowercase first letter)"
                ));
            }
        }
        
        return issues;
    }
    
    /**
     * Validate package structure.
     */
    private List<ValidationIssue> validatePackageStructure(String code) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        // Check if package is defined
        if (!code.contains("package ")) {
            issues.add(ValidationIssue.info(
                "No package declaration found. Consider adding 'package <Name> { ... }'"
            ));
        }
        
        // Check for proper package syntax
        Pattern packagePattern = Pattern.compile("package\\s+(\\w+)\\s*\\{");
        Matcher matcher = packagePattern.matcher(code);
        
        if (matcher.find()) {
            String packageName = matcher.group(1);
            
            // Check PascalCase for package names
            if (!Character.isUpperCase(packageName.charAt(0))) {
                issues.add(ValidationIssue.info(
                    "Package name '" + packageName + "' should use PascalCase"
                ));
            }
        }
        
        return issues;
    }
    
    /**
     * Extract all defined type names from code.
     */
    private Set<String> extractDefinedTypes(String code) {
        Set<String> types = new HashSet<>();
        
        // Pattern: keyword def TypeName
        Pattern defPattern = Pattern.compile("\\w+\\s+def\\s+(\\w+)");
        Matcher matcher = defPattern.matcher(code);
        
        while (matcher.find()) {
            types.add(matcher.group(1));
        }
        
        return types;
    }
    
    /**
     * Validation result containing validity status and issues.
     */
    public record ValidationResult(
        boolean isValid,
        List<ValidationIssue> issues
    ) {
        public boolean hasErrors() {
            return issues.stream().anyMatch(i -> "ERROR".equals(i.severity()));
        }
        
        public boolean hasWarnings() {
            return issues.stream().anyMatch(i -> "WARNING".equals(i.severity()));
        }
        
        public List<ValidationIssue> getErrors() {
            return issues.stream()
                .filter(i -> "ERROR".equals(i.severity()))
                .toList();
        }
        
        public List<ValidationIssue> getWarnings() {
            return issues.stream()
                .filter(i -> "WARNING".equals(i.severity()))
                .toList();
        }
    }
}
