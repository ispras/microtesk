from template import Template

class MiniMipsBaseTemplate(Template):
    def __init__(self):
        Template.__init__(self)
    
    def pre(self):
        
        self.section_text({'pa' : 0x0, 'va' : 0x0,'args' : ''})
        
    
    def post(self):
        pass
    
    def zero(self):
        pass
    
    def at(self,contents):
        pass
    

#template = MiniMipsBaseTemplate()
#template.generate()