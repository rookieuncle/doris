// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.nereids.trees.expressions.functions;

import org.apache.doris.catalog.FunctionSignature;
import org.apache.doris.nereids.trees.expressions.Cast;
import org.apache.doris.nereids.trees.expressions.Expression;
import org.apache.doris.nereids.trees.expressions.literal.IntegerLiteral;
import org.apache.doris.nereids.types.DecimalV3Type;
import org.apache.doris.nereids.types.coercion.Int32OrLessType;

import com.google.common.base.Preconditions;

/** ComputePrecisionForRound */
public interface ComputePrecisionForRound extends ComputePrecision {
    @Override
    default FunctionSignature computePrecision(FunctionSignature signature) {
        if (arity() == 1 && getArgumentType(0).isDecimalV3Type()) {
            DecimalV3Type argumentType = (DecimalV3Type) getArgumentType(0);
            return signature.withReturnType(DecimalV3Type.createDecimalV3Type(argumentType.getPrecision(), 0));
        } else if (arity() == 2 && getArgumentType(0).isDecimalV3Type()) {
            DecimalV3Type decimalType = (DecimalV3Type) getArgumentType(0);
            Expression floatLength = getArgument(1);
            Preconditions.checkArgument(floatLength.getDataType() instanceof Int32OrLessType
                    && (floatLength.isLiteral() || (
                            floatLength instanceof Cast && floatLength.child(0).isLiteral()
                                    && floatLength.child(0).getDataType() instanceof Int32OrLessType)),
                    "2nd argument of function round/floor/ceil/truncate must be literal");

            int scale;
            if (floatLength instanceof Cast) {
                scale = ((IntegerLiteral) floatLength.child(0)).getIntValue();
            } else {
                scale = ((IntegerLiteral) floatLength).getIntValue();
            }
            return signature.withReturnType(DecimalV3Type.createDecimalV3Type(decimalType.getPrecision(), scale));
        } else {
            return signature;
        }
    }
}
