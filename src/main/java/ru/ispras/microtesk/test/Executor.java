/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Executor.java, Aug 12, 2014 2:23:17 PM Andrei Tatarnikov
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.InstructionCall;
import ru.ispras.microtesk.model.api.state.IModelStateObserver;
import ru.ispras.microtesk.test.data.ConcreteCall;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.Output;

public final class Executor
{
    private static final class LabelEntry
    {
        private final Label label;
        private final int jumpPos;

        public LabelEntry(Label label, int jumpPos)
        {
            if (null == label)
                throw new NullPointerException();

            this.label = label;
            this.jumpPos = jumpPos;
        }
    }

    private static class LabelManager
    {
        private final Map<String, List<LabelEntry>> table;

        LabelManager()
        {
            this.table = new HashMap<String, List<LabelEntry>>();
        }

        public void add(Label label, int jumpPos)
        {
            if (null == label)
                throw new NullPointerException();

            final LabelEntry entry = new LabelEntry(label, jumpPos);

            final List<LabelEntry> entries;
            if (table.containsKey(label.getName()))
            {
                entries = table.get(label.getName());
            }
            else
            {
                entries = new ArrayList<LabelEntry>();
                table.put(label.getName(), entries);
            }

            entries.add(entry);
        }
        
        public void addAll(Collection<?> labels, int jumpPos)
        {
            if (null == labels)
                throw new NullPointerException();

            for (Object item : labels)
            {
                if (!(item instanceof Label))
                    throw new IllegalArgumentException(
                        item + " is not a Label object!");

                add((Label) item, jumpPos);
            }
        }

        public LabelEntry resolve(Label label)
        {
            if (null == label)
                throw new NullPointerException();

            if (!table.containsKey(label.getName()))
                return null;

            final List<LabelEntry> entries = 
                table.get(label.getName());

            // If there is only one entry, there is no other choice.
            if (1 == entries.size())
                return entries.get(0);

            // Find a label defined in the current block
            // Find a label defined in the closest child
            // Find a label defined in the closest parent
            // Find a label defined in the closest sibling

            return null;
        }
    }

    private final IModelStateObserver observer;
    private final boolean logExecution;

    public Executor(IModelStateObserver observer, boolean logExecution)
    {
        if (null == observer)
            throw new NullPointerException();

        this.observer = observer;
        this.logExecution = logExecution;
    }

    public void executeSequence(Sequence<ConcreteCall> sequence) throws ConfigurationException
    {
        if (null == sequence)
            throw new NullPointerException();

        // Remember all labels defined by the sequence and its positions.
        final LabelManager labelManager = new LabelManager();
        for (int index = 0; index < sequence.size(); ++index)
        {
            final ConcreteCall instr = sequence.get(index);
            
            final List<?> f_labels = toList(instr.getAttribute("f_labels"));
            labelManager.addAll(f_labels, index);

            final List<?> b_labels = toList(instr.getAttribute("b_labels"));
            labelManager.addAll(b_labels, index + 1);
        }
        
        int currentPos = 0;
        final int endPos = sequence.size();
        while (currentPos < endPos)
        {
            final ConcreteCall instr = sequence.get(currentPos);

            logOutputs(instr.getAttribute("b_runtime"));

            final InstructionCall call = instr.getExecutable();
            logText(call.getText());
            call.execute();

            logOutputs(instr.getAttribute("f_runtime"));

            // TODO: Support instructions with 2+ labels (needs API)
            final int ctStatus = observer.getControlTransferStatus();
            if (ctStatus > 0)
            {
                final List<?> instrLabels = 
                    toList(instr.getAttribute("labels"));
                
                // TODO: What to do if there are no labels?
                if (instrLabels.isEmpty())
                    throw new IllegalStateException();

                // First - label, Second - instruction argument name 
                final List<?> instrLabelEntry = toList(instrLabels.get(0));
                
                // target = inst.getAttribute("labels").first.first
                final Label targetLabel = (Label) instrLabelEntry.get(0);  
                if (null == targetLabel)
                {
                    System.out.println("Jump to nil label, transfer status: " + ctStatus);
                    currentPos++; // continue to the next instruction, no other idea
                }
                else
                {
                    final LabelEntry target = labelManager.resolve(targetLabel);
                    if (null == target)
                        throw new IllegalStateException("No label called" + targetLabel.getName() + "is defined");
                    
                    currentPos = target.jumpPos;
                    logLabelJump(target.label);
                }
            }
            else // If there are no transfers, continue to the next instruction
            {
                currentPos++;
            }
        }
    }

    private void logOutputs(Object o) throws ConfigurationException
    {
        if (null == o)
            return;

        final List<?> list = toList(o);
        for (Object item : list)
        {
            if (!(item instanceof Output))
                throw new IllegalArgumentException(
                    item + " is not an Output object!");

            final Output output = (Output) item;
            logText(output.evaluate(observer));
        }
    }
    
    private void logLabelJump(Label target)
    {
        logText("Jump (internal) to label: " + target.getUniqueName());
    }

    private void logText(String text)
    {
        if (logExecution)
            System.out.println(text);
    }

    private static List<?> toList(Object o)
    {
        if (!(o instanceof List))
            throw new IllegalArgumentException(
                o + " is not a List object.");

        return (List<?>) o;
    }
}

/*

class Executor

  def initialize(context, abstract_calls, is_log)
    @context = context
    @abstract_calls = abstract_calls
    @log_execution = is_log
  end
  
  def get_concrete_calls
    @final_sequences
  end

  def execute

    bl_iter = @abstract_calls
    
    # Preprocess labels
    @labels = Hash.new

    # look for labels in the sequences
    bl_iter.init()
    sn = 0
    sequences = Array.new
    
    while bl_iter.hasValue()
      seq = bl_iter.value()

      seq.each_with_index do |inst, i|
        #TODO check if sequences have nulls?
        if inst == nil
          next
        end
        
        f_labels = inst.getAttribute("f_labels")
        b_labels = inst.getAttribute("b_labels")

        #process labels
      
        if f_labels.is_a? Array
          f_labels.each do |label|
            @labels[label] = [sn, i + 1]
          end
        end
        
        if b_labels.is_a? Array
          b_labels.each do |label|
            @labels[label] = [sn, i]
          end 
        end        
      end
      sn += 1
      sequences.push seq
      
      bl_iter.next()
    end
    
    # Execute and generate data in the process
    @final_sequences = Array.new(sequences.length)
    @final_sequences.each_with_index do |sq, i| 
      @final_sequences[i] = nil 
    end
    
    cur_seq = 0
    continue = true
    label = nil
    
    # puts @labels.to_s
    seq.each_with_index do |inst, i|
        #TODO check if sequences have nulls?
        if inst == nil
          next
        end
        
        f_labels = inst.getAttribute("f_labels")
        b_labels = inst.getAttribute("b_labels")

        #process labels
      
        if f_labels.is_a? Array
          f_labels.each do |label|
            @labels[label] = [sn, i + 1]
          end
        end
        
        if b_labels.is_a? Array
          b_labels.each do |label|
            @labels[label] = [sn, i]
          end 
        end        
      end
      sn += 1
      sequences.push seq
      
      bl_iter.next()
    # execution loop
    while continue && cur_seq < sequences.length
      fin, label = exec_sequence(sequences[cur_seq], @final_sequences[cur_seq], cur_seq, label)
      
      if @final_sequences[cur_seq] == nil && cur_seq < sequences.length
        @final_sequences[cur_seq] = fin
      end
      
      if label == nil
        goto = cur_seq + 1
      else
        search_label = label
        while @labels[search_label] == nil && search_label != nil
          search_label = search_label.getParentLabel
        end
        
        result = @labels[search_label]
        if result == nil
          goto = cur_seq + 1
          puts "Label " + label.getName + " doesn't exist"
        else
          label = search_label
          goto = result.first
        end
      end      
      
      if (goto >= sn + 1) or (goto == -1 && cur_seq >= sn)
        continue = false
      else
        cur_seq = goto
      end
      
    end
      
    # Generate the remaining sequences  
    @final_sequences.each_with_index do |s, i|
      if s == nil && i < sequences.length
#        if sequences[i] == nil
#          puts "what the fuck " + i.to_s
#        end
        @final_sequences[i] = Engine.generate_data sequences[i]
      end
    end
    
  end
  
  def exec_sequence(seq, gen, id, label)
    r_gen = gen
    if gen == nil
      # TODO NEED EXCEPTION HANDLER
      r_gen = Engine.generate_data seq
    end
    
    labels = Hash.new
    
    r_gen.each_with_index do |inst, i|
      f_labels = inst.getAttribute("f_labels")
      b_labels = inst.getAttribute("b_labels")
      
      #process labels
      
      if f_labels.is_a? Array
        f_labels.each do |f_label|
          labels[f_label] = i + 1
          # puts "Registered f_label " + f_label
        end
      end
      
      if b_labels.is_a? Array
        b_labels.each do |b_label|
          labels[b_label] = i
          # puts "Registered b_label " + b_label
        end        
      end
    end
    
    cur_inst = 0
    
    if label != nil
      cur_inst = labels[label]
      # puts label.to_s
      # puts labels.to_s
    end
    
    total_inst = r_gen.length

    continue = true

    jump_target = nil

    while continue && cur_inst < total_inst

      inst = r_gen[cur_inst]

      print_debug inst.getAttribute("b_runtime")
      
      exec = inst.getExecutable()
      print_text exec.getText()
      exec.execute()

      print_debug inst.getAttribute("f_runtime")

      # Labels
      jump = StateObserver.control_transfer_status

      # TODO: Support instructions with 2+ labels (needs API)
      
      if jump > 0
        target = inst.getAttribute("labels").first.first
        if target == nil
          puts "Jump to nil label, transfer status: " + jump.to_s
        elsif labels.has_key? target
          cur_inst = labels[target]
          print_label_jump target
          next
        else
          jump_target = target
          print_label_jump target
          break
        end
      end
      
      # If there weren't any jumps, continue on to the next instruction
      cur_inst += 1
    end
    
    [r_gen, jump_target]
    
  end

  def print_text(text)
    if @log_execution
       puts text
    end
  end

  def print_debug(debug_arr)
    if @log_execution and debug_arr.is_a? Array
      debug_arr.each do |debug|
        text = debug.evaluate @context.get_state_observer
        if nil != text
          puts text
        end 
      end
    end
  end

  def print_label_jump(target)
    if @log_execution
      puts "Jump (internal) to label: " + target.to_s
    end
  end

end

*/



