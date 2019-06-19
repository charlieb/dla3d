import bpy, math, ast

# filename = "/home/charlieb/src/dla3d/to_blend.py"
# exec(compile(open(filename).read(), filename, 'exec')) 

def cylinder_between(x1, y1, z1, x2, y2, z2, r):

  dx = x2 - x1
  dy = y2 - y1
  dz = z2 - z1    
  dist = math.sqrt(dx**2 + dy**2 + dz**2)

  bpy.ops.mesh.primitive_cylinder_add(
      radius = r, 
      depth = dist,
      location = (dx/2 + x1, dy/2 + y1, dz/2 + z1)   
  ) 

  phi = math.atan2(dy, dx) 
  theta = math.acos(dz/dist) 

  bpy.context.object.rotation_euler[1] = theta 
  bpy.context.object.rotation_euler[2] = phi

def cyls(filename):
    radius = 0.1
    with open(filename, 'r') as f:
        cs = ast.literal_eval(f.read())

    for [[x1, y1, z1], [x2, y2, z2]] in cs:
        bpy.ops.mesh.primitive_ico_sphere_add(location=(x1, y1, z1), size=radius)
        cylinder_between(x1, y1, z1,
                         x2, y2, z2, radius)

cyls("/home/charlieb/src/dla3d/test.points")


