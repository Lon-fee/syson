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

/**
 * Standalone test for SysML Code Validator.
 * Run this directly without Maven build.
 */
public class ValidatorTest {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("SysML Code Validator - Standalone Test");
        System.out.println("========================================\n");
        
        SysMLCodeValidator validator = new SysMLCodeValidator();
        
        // Test 1: Valid code
        System.out.println("Test 1: Valid Code");
        System.out.println("------------------");
        String validCode = "part def Vehicle {\n" +
                          "    attribute mass : Real;\n" +
                          "    part engine : Engine;\n" +
                          "}";
        testCode(validator, validCode);
        
        // Test 2: Missing semicolon
        System.out.println("\nTest 2: Missing Semicolon");
        System.out.println("-------------------------");
        String missingSemicolon = "part def Vehicle {\n" +
                                 "    attribute mass : Real\n" +
                                 "}";
        testCode(validator, missingSemicolon);
        
        // Test 3: Unbalanced braces
        System.out.println("\nTest 3: Unbalanced Braces");
        System.out.println("-------------------------");
        String unbalancedBraces = "part def Vehicle {\n" +
                                 "    attribute mass : Real;\n";
        testCode(validator, unbalancedBraces);
        
        // Test 4: Invalid keyword
        System.out.println("\nTest 4: Invalid Keyword");
        System.out.println("-----------------------");
        String invalidKeyword = "invalid def Vehicle {}";
        testCode(validator, invalidKeyword);
        
        // Test 5: Wrong naming convention
        System.out.println("\nTest 5: Naming Convention");
        System.out.println("-------------------------");
        String wrongNaming = "part def vehicle {}";  // Should be PascalCase
        testCode(validator, wrongNaming);
        
        // Test 6: Complex valid code
        System.out.println("\nTest 6: Complex Valid Code");
        System.out.println("--------------------------");
        String complexCode = "package AutomotiveSystems {\n" +
                           "    part def Engine {\n" +
                           "        attribute power : Real;\n" +
                           "        attribute cylinders : Integer;\n" +
                           "    }\n" +
                           "    \n" +
                           "    part def Vehicle {\n" +
                           "        attribute mass : Real;\n" +
                           "        part engine : Engine;\n" +
                           "        part wheels : Wheel[4];\n" +
                           "    }\n" +
                           "}";
        testCode(validator, complexCode);
        
        System.out.println("\n========================================");
        System.out.println("All tests completed!");
        System.out.println("========================================");
    }
    
    private static void testCode(SysMLCodeValidator validator, String code) {
        System.out.println("Code:");
        System.out.println(code);
        System.out.println();
        
        SysMLCodeValidator.ValidationResult result = validator.validate(code);
        
        System.out.println("Valid: " + result.isValid());
        System.out.println("Has Errors: " + result.hasErrors());
        System.out.println("Has Warnings: " + result.hasWarnings());
        
        if (!result.issues().isEmpty()) {
            System.out.println("\nIssues:");
            result.issues().forEach(issue -> {
                System.out.println("  [" + issue.severity() + "] " + issue.message());
                if (issue.suggestion() != null) {
                    System.out.println("      → " + issue.suggestion());
                }
            });
        }
    }
}
