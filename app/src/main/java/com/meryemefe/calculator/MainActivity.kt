package com.meryemefe.calculator

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),  View.OnClickListener{

    private var inputs = ArrayList<String>()    // ArrayList to hold operators and operands in order
    private var result = 0.0    // Result initialized with 0
    private val maxNumberOfDigits = 26  // final value to show maximum number of digits for result TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initializes the result as 0
        textResult.text = "0"

        // Add onClick methods to all the buttons
        button0.setOnClickListener(this)
        button7.setOnClickListener(this)
        button8.setOnClickListener(this)
        button9.setOnClickListener(this)
        button4.setOnClickListener(this)
        button5.setOnClickListener(this)
        button6.setOnClickListener(this)
        button1.setOnClickListener(this)
        button2.setOnClickListener(this)
        button3.setOnClickListener(this)
        buttonAC.setOnClickListener(this)
        buttonPlus.setOnClickListener(this)
        buttonMinus.setOnClickListener(this)
        buttonMultiply.setOnClickListener(this)
        buttonDivide.setOnClickListener(this)
        buttonEqual.setOnClickListener(this)

    }


    /**
     * This method calls proper method for the clicked button.
     * @param v : View as Button
     */
    override fun onClick(v: View?) {

        when(val b = v as Button){
            buttonAC -> reset()
            buttonPlus -> addOperator("+")
            buttonMinus -> addOperator("-")
            buttonMultiply -> addOperator("x")
            buttonDivide -> addOperator("÷")
            buttonEqual -> calculate()
            else -> addOperand(b.text.toString())
        }
    }

    /**
     * This method resets both text areas and current operations.
     */
    private fun reset(){
        textCalculation.text = ""
        textResult.text = "0"
        inputs.clear()
    }

    /**
     * This method adds an operand to inputs ArrayList.
     * It also checks overflow exceptions
     * @param operand : String
     */
    private fun addOperand( operand:String){

        // If the last input is a number, append the new clicked number to the end of the last input.
        // Otherwise, add the number as a new input.
        if (inputs.size == 1 || (inputs.size > 1 && inputs[inputs.size-1].toLongOrNull() != null) ) {
            if ("${inputs[inputs.size - 1]}$operand".toLongOrNull() != null){
                inputs[inputs.size - 1] = "${inputs[inputs.size - 1]}$operand".toLong().toString()
            }
        } else {
            inputs.add(operand)
        }

        updateCalculationTextView()
    }

    /**
     * This method adds operator to inputs ArrayList.
     * @param operator: String -> +, -, x, ÷
     */
    private fun addOperator( operator:String){

        // If there is only one in input and it is an operand, you can only change it to (-).
        // Otherwise, clear the input to show it becomes a positive number.
        if (inputs.size == 1 && inputs[0].toLongOrNull() == null) {
            if (operator == "-") {
                inputs[0] = "-"
            } else {
                inputs.clear()
            }
        } else if (inputs.size > 0) {
            // If last input is not a number, change the last operator.
            if( inputs[inputs.size-1].toLongOrNull() == null){
                inputs[inputs.size - 1] = operator
            }
            // If the last input is a number, add operator as a new input.
            else {
                inputs.add(operator)
            }
        } else {
            // If there is no input, you can just put (-) as a signature of negative number.
            if(operator == "-"){
                inputs.add("-")
            }
        }
        updateCalculationTextView()
    }


    /**
     * This method converts inputs ArrayList to real operations, and calculate the result.
     * It also checks if arithmetic operation error.
     */
    private fun calculate(){

        // If last input is an operand, remove it so that you can calculate easily.
        if (inputs.size > 0 && inputs[inputs.size-1].toLongOrNull() == null){
            inputs.removeLast()
            updateCalculationTextView()
        }

        // If there is no input, don't calculate anything.
        if (inputs.size == 0){
            return
        }

        // First, do multiplication and division operations.
        try {
            performMultiplicationAndDivision()
        } catch (e : ArithmeticException){
            textResult.text = getString(R.string.error)
            inputs.clear()
            return
        }

        // After, do summation and subtraction operations.
        performSummationAndSubtraction()

        // Display result on result textView
        displayResult()

        // Since operations were calculated, reset inputs ArrayList
        inputs.clear()
    }


    /**
     * This method searches multiplication and division operations
     * and performs them orderly.
     * If there is division by zero error, it throws exception.
     */
    private fun performMultiplicationAndDivision(){

        // Define helper variables
        var temp:Double
        var indexOfMultiply:Int
        var indexOfDivide:Int
        var index:Int

        // This loop checks all 'x' and '÷' operations.
        while (true){

            // Find indices of first 'x' and first '÷' operators.
            indexOfMultiply = inputs.indexOf("x")
            indexOfDivide = inputs.indexOf("÷")

            // If index of 'x' is smaller, firstly perform multiplication.
            if ( indexOfMultiply > 0 && (indexOfDivide == -1 || (indexOfDivide != -1 && indexOfMultiply < indexOfDivide)) ){
                index = indexOfMultiply
                temp = inputs[index-1].toDouble() * inputs[index+1].toDouble()
            }
            // If index of '÷' is smaller, firstly perform division.
            else if ( indexOfDivide > 0 && (indexOfMultiply == -1 || (indexOfMultiply != -1 && indexOfDivide < indexOfMultiply)) ){
                index = indexOfDivide
                if (inputs[index+1].toInt() == 0){
                    throw ArithmeticException("Division by zero")
                }
                temp = inputs[index-1].toDouble() / inputs[index+1].toDouble()
            }
            // If there is neither 'x' nor '÷', break the loop.
            else {
                break
            }

            // If you find 'x' or '÷', replace this operation with its result.
            replaceSmallOperationWithResult(index-1, temp)
        }
    }

    /**
     * This method searches summation and subtraction operations
     * and performs them orderly.
     */
    private fun performSummationAndSubtraction(){

        // Define helper variables
        var temp:Double
        var indexOfPlus:Int
        var indexOfMinus:Int
        var index:Int

        // This loop checks all '+' and '-' operations.
        while (true){

            // Find indices of first '+' and first '-' operators.
            indexOfPlus = inputs.indexOf("+")
            indexOfMinus = inputs.indexOf("-")

            // If index of '+' is smaller, firstly perform summation.
            if ( indexOfPlus > 0 && (indexOfMinus == -1 || (indexOfMinus != -1 && indexOfPlus < indexOfMinus)) ){
                index = indexOfPlus
                temp = inputs[index-1].toDouble() + inputs[index+1].toDouble()
            }
            // If index of '-' is smaller, firstly perform subtraction.
            else if ( indexOfMinus > 0 && (indexOfPlus == -1 || (indexOfPlus != -1 && indexOfMinus < indexOfPlus)) ){
                index = indexOfMinus
                temp = inputs[index-1].toDouble() - inputs[index+1].toDouble()
            }
            // If there is neither '+' nor '-', break the loop.
            else {
                break
            }

            // If you find '+' or '-', replace this operation with its result.
            replaceSmallOperationWithResult(index-1, temp)
        }
    }

    /**
     * This method displays current operations on calculation textView.
     */
    private fun updateCalculationTextView(){
        textCalculation.text = inputs.joinToString().replace(",", "")
    }

    /**
     * This method display the result on result textView.
     * It also format the result so that number of digits won't exceed max number of digits (26).
     */
    @SuppressLint("SetTextI18n")
    private fun displayResult(){
        result = inputs[0].toDouble()

        when {
            // If there is no fractional part, display it in long format instead of double format.
            result == result.toLong().toDouble() -> {
                textResult.text = result.toLong().toString()
            }
            // If there is fractional part which exceeds maximum number of digits, format them so that it fits on the TextView.
            result.toString().length > maxNumberOfDigits -> {
                val numOfFractionalDigit = maxNumberOfDigits - result.toString().indexOf(".") - 1
                textResult.text = "%.${numOfFractionalDigit}f".format(result)
            }
            // If it doesn't exceeds, display it without formatting.
            else -> {
                textResult.text = result.toString()
            }
        }

        // Add '=' to the end of result textView.
        textCalculation.text = "${textCalculation.text}="
    }

    /**
     * This method removes three elements from inputs ArrayList,
     * and it adds result of these three elements.
     * Eg.: Remove 3 + 5, Add 8
     * @param index : Int -> the index of the element which is first to be removed
     * @param tempResult : Double -> the result which is inserted to ArrayList
     */
    private fun replaceSmallOperationWithResult( index: Int, tempResult: Double){
        inputs.removeAt(index)
        inputs.removeAt(index)
        inputs.removeAt(index)
        inputs.add(index, tempResult.toString())
    }
    
}