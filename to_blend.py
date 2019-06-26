import bpy
import math, mathutils, random, ast

from mathutils import *
from math import cos, sin, pi, sqrt, radians

# filename = "/home/charlieb/src/dla3d/to_blend.py"
# exec(compile(open(filename).read(), filename, 'exec')) 

GLOBAL_ZERO_PADDING = 6                 # The number of zeros to pad strings with when converting INTs to STRINGs.

def returnNameForNumber(passedFrame):
    frame_number = str(passedFrame)
    post_fix = frame_number.zfill(GLOBAL_ZERO_PADDING)
    return post_fix

def createEmpty(passedScene, passedName="Empty"):
    try:
        ob = bpy.data.objects.new(passedName, None)
        passedScene.objects.link(ob)
    except:
        ob = None
    return ob

def returnMeshCreated (name, verts, faces):
    # Creates mesh
    # name - name of object to create
    # verts - a list of vertex tuples
    # faces - a list of face tuples

    # Actually create mesh and object
    mesh = bpy.data.meshes.new(name)
    #obj = bpy.data.objects.new(name, mesh)

    # add verts & faces to object
    mesh.from_pydata(verts, [], faces)
    mesh.update(calc_edges=True)    
    return mesh    

def create_end_faces(verts_list, sides, debug=False):
    # Create End Faces
    # verts_list - list of vertices
    # sides - number of sides to mesh
    # debug - if true prints values from this function to console
    
    # returns:
    # faces - a list of tuples defining the end faces
    
    faces = []
    num_of_verts = len(verts_list)
    faces_temp = []

    if sides > 4:
        # more than 4 sides, so replace last list item (center vert) with first list item 
        # for looping and building faces
        center_vert = verts_list[num_of_verts - 1]
        verts_list[num_of_verts - 1] = verts_list[0]

        for index in range(int(num_of_verts - 1)):
            faces_temp.append(verts_list[index])
            faces_temp.append(verts_list[index + 1])
            faces_temp.append(center_vert)
            faces.append(tuple(faces_temp))
            faces_temp = []
    
    else:
        # create 1 end face
        for index in range(num_of_verts):
            faces_temp.append(verts_list[index])
        faces.append(tuple(faces_temp))               
    return faces

def create_side_faces(front_verts, back_verts, debug=False):
    # Create side faces - simple bridging of front_verts & back_verts vertices,
    #                     both front_verts & back_verts must be ordered in same direction
    #                     with respect to y-axis
    # front_verts - a list of front face vertices
    # back_verts - a list of back face vertices
    # debug - if true prints values from this function to console
    
    # returns:
    # new_faces - a list of tuples defining the faces bridged between front_verts & back_verts

    # Number of faces to create
    num_of_faces = (len(front_verts))
    new_faces = []
    
    # add first value to end of lists for looping
    front_verts.append(front_verts[0])
    back_verts.append(back_verts[0])
    
    # Build the new_faces list with tuples defining each face    
    for index in range(num_of_faces):
        facestemp = (front_verts[index], front_verts[index+1], back_verts[index+1], back_verts[index])
        new_faces.append(facestemp)
    return new_faces

def calc_end_verts(size, y_off, passedSides, debug=False):
    # Calculates vertex location for end of mesh
    # End vertex location is calculated by find points on a circle.
    # Currently only the z-size is used to calculate radius of circle,
    #   code calling this function call with a size of (1, y-size, z-size).
    # Needs to be improved to work with a x-value and work based on
    #   an ellipse, this will allow it to work with both
    #   x & z values to produce retangular shapes - TODO
    
    # size - tuple of x,y,z dimensions of mesh to create
    # y_off - y offset - y location to create vertices at
    # sides - number of sides to create vertices for
    # debug - if true prints values from this function to console
    
    # returns:
    # verts - a list of tuples of the x,y,z location of each vertex
    
    verts = []
    sides = passedSides
    if sides < 2: sides = 2
    # divide number of sides by 2 for testing for even or odd number of sides
    sides_test = sides / 2
    #calculate angle for where to create vertices
    angle = 360 / sides
    
    if sides_test.is_integer():
        # even number of sides - calcuate offset angle
        angle_off = angle / 2       
    else:
        # odd number of sides - set offset to 90 so it points in the world's positive z-axis,
        # as 0 would point at the world's positive x-axis
        angle_off = 90
                
    # calcuate radius according to size 
    # (x squared) + (z squared) = (r squared)
    x_sqr = pow(size[0], 2)
    z_sqr = pow(size[2], 2)
    radius = math.sqrt(x_sqr + z_sqr)
    radius /= 2
        
    # Create vertices by calculation    
    for index in range (sides):
        rads = math.radians((index * angle) + angle_off)
        
        # calcuate x position
        x_pos = radius * round(math.cos(rads),3)
        x_pos = round(x_pos, 3)
        # calcuate z position
        z_pos = radius * round(math.sin(rads),3)
        z_pos = round(z_pos, 3)
        verts.append((x_pos, y_off, z_pos))
        
    if sides > 4:
        # more than 4 sides and solid mesh - add center vertice
        verts.append((0, y_off, 0))
         
    return verts
    
def create_multi_side_box(size, sides, debug=False):
    # Creates a multi-sided box
    # size - tuple of x,y,z dimensions of box
    # sides - number of sides to mesh - must be >= 3 - not error checked
    # debug - if true prints values from this function to console
    
    # returns: 
    # verts_final - a list of tuples of the x, y, z, location of each vertice
    # faces_final - a list of tuples of the vertices that make up each face
   
    # Get y offset of vertices from center
    y_off = size[1] / 2
    # Create temporarylists to hold vertices locations
    verts_front_temp=[]
    verts_back_temp=[]    
        
    # Create front vertices by calculation
    verts_front_temp = calc_end_verts(size, 0+y_off, sides, debug)
    
    # Create back vertices by calculation
    verts_back_temp = calc_end_verts(size, 0-y_off, sides, debug)
    
    # Combine all vertices into a final list of tuples
    verts_final = verts_front_temp + verts_back_temp   
               
    # Create front face
    faces_front_temp = []
    verts_front_list = []
    numofverts = len(verts_front_temp)
    
    # Build vertex list
    for index in range(numofverts):
        verts_front_list.append(index)
       
    faces_front_temp = create_end_faces(verts_front_list, sides, debug) 
    
    # Create back face
    faces_back_temp = []
    verts_back_list = []
    numofverts = len(verts_back_temp)
    
    # Build vertex list
    for index in range(numofverts):
        verts_back_list.append(index + len(verts_back_temp))
        
    faces_back_temp = create_end_faces(verts_back_list, sides, debug)

    # Create side faces
    faces_side_temp = create_side_faces(verts_front_list, verts_back_list, debug)
    
    # Combine all faces 
    faces_final = faces_front_temp + faces_back_temp + faces_side_temp
    return verts_final, faces_final


def cylinder_between(x1, y1, z1, x2, y2, z2, r, ob_name, mt_parent):

    dx = x2 - x1
    dy = y2 - y1
    dz = z2 - z1    
    dist = math.sqrt(dx**2 + dy**2 + dz**2)

    verts, faces = create_multi_side_box((r,dist,r), 10) # x,y,z dimentions of box, sides
    me_new = returnMeshCreated("me_%s" % ob_name, verts, faces)
    ob = bpy.data.objects.new(ob_name, me_new)
    ob.location = (dx/2 + x1, dy/2 + y1, dz/2 + z1)

    phi = math.atan2(dy, dx) 
    theta = math.acos(dz/dist) 
    ob.rotation_mode = 'XYZ'
    ob.rotation_euler[0] = math.pi / 2
    ob.rotation_euler[1] = theta
    ob.rotation_euler[2] = phi
    print(ob.rotation_euler)
    ob.parent = mt_parent

def cyls(filename):
    scene = bpy.data.scenes[0]
    parent_name = "mt_Parent"
    mt_parent = bpy.data.objects.get(parent_name)
    if mt_parent == None:
        mt_parent = createEmpty(scene,parent_name)

    radius = 0.1
    with open(filename, 'r') as f:
        cs = ast.literal_eval(f.read())

    for i, [[x1, y1, z1], [x2, y2, z2]] in enumerate(cs):
        ob_name = "%s.%s" % ("myCylinder", returnNameForNumber(i))
        #bpy.ops.mesh.primitive_ico_sphere_add(location=(x1, y1, z1), size=radius)
        cylinder_between(x1, y1, z1,
                         x2, y2, z2, radius, 
                         ob_name, mt_parent)

    for n in range(i+1):
        ob_name = "%s.%s" % ("myCylinder", returnNameForNumber(n))
        print("Linking %s." % ob_name)
        ob = bpy.data.objects.get(ob_name)
        if ob != None:
            try:
                scene.objects.link(ob)
            except:
                pass
