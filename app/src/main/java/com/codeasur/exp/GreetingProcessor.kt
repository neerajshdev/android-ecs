package com.codeasur.exp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*

class GreetingProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("com.codeasur.exp.GenerateGreeting")
        symbols.forEach { it.accept(Visitor(), Unit) }
        return emptyList()
    }

    inner class Visitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val className = classDeclaration.simpleName.asString()
            val packageName = classDeclaration.packageName.asString()
            val file = codeGenerator.createNewFile(
                Dependencies(false, classDeclaration.containingFile!!),
                packageName,
                "${className}Greeting"
            )

            file.writer().use {
                it.write(
                    """
                package $packageName
                
                class ${className}Greeting {
                    fun greet(): String {
                        return "Hello, $className!"
                    }
                }
                """.trimIndent()
                )
            }

        }
    }
}

class GreetingProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return GreetingProcessor(environment.codeGenerator, environment.logger)
    }
}